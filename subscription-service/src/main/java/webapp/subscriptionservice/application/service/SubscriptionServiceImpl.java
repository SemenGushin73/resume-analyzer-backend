package webapp.subscriptionservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.project.exception.PaymentGatewayException;
import ru.kata.project.exception.ResourceNotFoundException;

import ru.kata.project.model.*;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;
import webapp.subscriptionservice.application.mapper.SubscriptionMapper;

import webapp.subscriptionservice.application.util.SubscriptionMetaJsonHelper;
import webapp.subscriptionservice.domain.model.*;
import webapp.subscriptionservice.domain.repository.SubscriptionHistoryRepository;
import webapp.subscriptionservice.domain.repository.UserSubscriptionRepository;
import webapp.subscriptionservice.domain.service.PaymentGatewayService;
import webapp.subscriptionservice.domain.service.SubscriptionService;
import webapp.subscriptionservice.infrastructure.persistence.entity.SubscriptionHistoryEntity;
import webapp.subscriptionservice.infrastructure.persistence.entity.UserSubscriptionEntity;
import webapp.subscriptionservice.infrastructure.persistence.mapper.SubscriptionHistoryMapper;
import webapp.subscriptionservice.infrastructure.persistence.mapper.UserSubscriptionMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Основной класс с бизнес-логикой, связанной с управлением подписками пользователей.
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final SubscriptionMapper subscriptionMapper;
    private final ObjectMapper objectMapper;
    private final UserSubscriptionMapper userSubscriptionPersistenceMapper;
    private final SubscriptionHistoryMapper subscriptionHistoryPersistenceMapper;
    private final SubscriptionMetaJsonHelper subscriptionMetaJsonHelper;

    /**
     * Создание новой подписки. Метод выполняет следующие шаги:
     * Проверяет отсутствие активной подписки у пользователя.
     * Инициализирует (создает или обновляет) доменную модель `UserSubscription` со статусом `INACTIVE`.
     * Записывает событие начала создания подписки в историю.
     * Взаимодействует с `PaymentGatewayService` для создания платежной сессии.
     * Обновляет доменную модель подписки данными, полученными от платежного шлюза, и сохраняет ее.
     * Записывает успешное завершение создания платежной сессии в историю.
     * В случае ошибки взаимодействия с платежным шлюзом, логирует неудачу и сохраняет запись в историю.
     *
     * @param userId Идентификатор пользователя, для которого создается подписка.
     * @param type   Тип подписки (`BASIC` или `PRO`).
     * @return DTO с информацией о созданной платежной сессии (ID платежа, URL подтверждения, QR-код).
     * @throws IllegalArgumentException Если у пользователя уже есть активная подписка.
     * @throws ExecutionException       Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом.
     * @throws InterruptedException     Если текущий поток был прерван во время ожидания ответа от шлюза.
     * @throws PaymentGatewayException  Если платежный шлюз возвращает ошибку (транслируется из `PaymentGatewayService`).
     */
    @Transactional
    @Override
    public PaymentSessionResponseDto createSubscription(UUID userId, SubscriptionType type)
            throws ExecutionException, InterruptedException {
        if (userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).isPresent()) {
            throw new IllegalArgumentException("У пользователя уже есть активная подписка");
        }
        final UserSubscriptionEntity userSubscriptionEntity = initializeUserSubscriptionEntity(userId, type);

        logSubscriptionInitiation(userSubscriptionEntity.getUserId(), userSubscriptionEntity.getType(), userSubscriptionEntity.getId());

        final PaymentGatewayService.PaymentSessionResponse paymentResponse = createPaymentSession(userId, type);

        updateAndSaveSubscriptionAfterPaymentSession(userSubscriptionEntity, paymentResponse);

        logPaymentSessionSuccess(userSubscriptionEntity.getUserId(), userSubscriptionEntity.getType(), paymentResponse);

        return new PaymentSessionResponseDto(paymentResponse.paymentId(),
                paymentResponse.confirmationUrl(), paymentResponse.qrCodeData());
    }

    /**
     * **Отмена активной подписки. Метод выполняет следующие шаги:
     * Находит активную подписку пользователя.
     * Записывает событие инициации отмены в историю.
     * Пытается отменить платеж через платежный шлюз (если `externalPaymentId` существует).
     * Обновляет статус подписки на `INACTIVE` (используя доменную логику `cancel()`) и сохраняет её.
     * Записывает результат отмены (успех или предупреждение) в историю.
     * В случае ошибки отмены платежа через шлюз, логирует ошибку, но все равно переводит подписку в статус `INACTIVE`
     *
     * @param userId Идентификатор пользователя, чья подписка должна быть отменена.
     * @throws ResourceNotFoundException Если активная подписка для указанного пользователя не найдена.
     * @throws ExecutionException        Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом.
     * @throws InterruptedException      Если текущий поток был прерван во время ожидания ответа от шлюза.
     * @throws PaymentGatewayException   Если платежный шлюз возвращает ошибку (транслируется из `PaymentGatewayService`).
     */
    @Transactional
    @Override
    public void cancelSubscription(UUID userId) throws ExecutionException, InterruptedException {

        final UserSubscriptionEntity userSubscriptionEntityToCancel = userSubscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Активная подписка не найдена: " + userId));

        final UserSubscription domainSubscriptionToCancel = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntityToCancel);

        logCancelInitiation(userId, domainSubscriptionToCancel);

        try {
            handleGatewayCancellation(userId, domainSubscriptionToCancel);

            finalizeSubscriptionCancellation(userId, userSubscriptionEntityToCancel);
            log.info("Отменена подписка для пользователя userId: {}", userId);
        } catch (PaymentGatewayException e) {
            log.error("Ошибка отмены платежа в шлюзе для пользователя userId {}: {}", userId, e.getMessage());
            logCancellationFailure(userId, userSubscriptionEntityToCancel, e);
            throw e;
        }
    }

    /**
     * Получение истории подписок. Возвращает список всех записей истории подписок для указанного пользователя.
     * Данные из репозитория (сущности) преобразуются сначала в доменные модели, а затем в DTO перед возвратом клиенту.
     *
     * @param userId Идентификатор пользователя.
     * @return Список DTO `SubscriptionHistoryDto`, представляющих историю подписок пользователя,
     * отсортированный по дате создания в убывающем порядке.
     */
    @Transactional(readOnly = true)
    @Override
    public List<SubscriptionHistoryDto> getSubscriptionHistory(UUID userId) {
        final List<SubscriptionHistoryEntity> historyEntities = subscriptionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return historyEntities.stream()
                .map(subscriptionHistoryPersistenceMapper::toDomainModel)
                .map(subscriptionMapper::toHistoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение активной подписки. Возвращает информацию об активной подписке пользователя, если она существует.
     * Данные из репозитория (сущность) преобразуются сначала в доменную модель, а затем в DTO перед возвратом клиенту.
     *
     * @param userId Идентификатор пользователя.
     * @return `Optional` с `UserSubscriptionDto`, представляющим активную подписку пользователя.
     * Если активная подписка не найдена, возвращается пустой `Optional`.
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<UserSubscriptionDto> getActiveSubscription(UUID userId) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(userSubscriptionPersistenceMapper::toDomainModel)
                .map(subscriptionMapper::toUserSubscriptionDto);
    }

    /**
     * **Обработка коллбэка платежного шлюза о статусе платежа (успех, неудача и т.д.).
     * Метод выполняет следующие шаги:
     * Находит соответствующую подписку по `paymentId`.
     * Парсит и обновляет метаданные коллбэка.
     * На основе статуса платежа из шлюза:
     * **"succeeded"**: Активирует подписку (используя доменную логику `activate()`),
     * устанавливает дату истечения и логирует успешное завершение.
     * **"failed"**: Отменяет подписку (используя доменную логику `cancel()`),
     * логирует неудачу.
     * **По умолчанию (другие статусы)**: Устанавливает подписку в статус `INACTIVE`
     * и логирует неизвестный/необрабатываемый статус.
     * Сохраняет обновленную подписку.
     * Создает запись в истории подписок о результате коллбэка.
     *
     * @throws ResourceNotFoundException Если подписка с указанным `paymentId` не найдена в системе.
     */
    @Transactional
    @Override
    public void handlePaymentCallback(String paymentId, PaymentStatus status, String metadataJson) {
        log.info("Обработка коллбэка о платеже для paymentId: {}, status: {}", paymentId, status);

        final UserSubscriptionEntity userSubscriptionEntity = userSubscriptionRepository.findByExternalPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.error("Не найдена подписка для платежа payment ID: {}", paymentId);
                    return new ResourceNotFoundException("Не найдена подписка для платежа payment ID: " + paymentId);
                });

        final UserSubscription subscriptionDomain = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntity);

        final ObjectNode metaNode = subscriptionMetaJsonHelper.prepareCallbackMetaNode(
                paymentId, status.name(), metadataJson, subscriptionDomain.getId()
        );

        final SubscriptionEventType historyEventType;
        final String logMessage;

        switch (status) {
            case SUCCEEDED:
                subscriptionDomain.activate(LocalDateTime.now().plusMonths(1));
                historyEventType = SubscriptionEventType.COMPLETED;
                logMessage = "Оплата прошла успешно. Подписка активирована.";
                metaNode.put("action", "Активация подписки");
                break;
            case FAILED:
                subscriptionDomain.cancel();
                historyEventType = SubscriptionEventType.FAILED;
                logMessage = "Ошибка или отмена платежа. Подписка помечена как неактивная.";
                metaNode.put("action", "Автоматическая деактивация");
                break;
            default:
                subscriptionDomain.setStatus(SubscriptionStatus.INACTIVE);
                historyEventType = SubscriptionEventType.FAILED;
                logMessage = "Неизвестный статус платежа: " + status.name() + ". Подписка помечена как неактивная.";
                metaNode.put("action", "Деактивация по неизвестному/необрабатываемому статусу");
                metaNode.put("reason", "Неизвестный статус: " + status.name());
        }

        log.info("{} Пользователь: {}, Подписка: {}", logMessage, subscriptionDomain.getUserId(), subscriptionDomain.getType());

        userSubscriptionEntity.setStatus(subscriptionDomain.getStatus());
        userSubscriptionEntity.setExpiresAt(subscriptionDomain.getExpiresAt());
        userSubscriptionEntity.setStartedAt(subscriptionDomain.getStartedAt());

        userSubscriptionRepository.save(userSubscriptionEntity);

        createHistoryEntry(new SubscriptionHistory(subscriptionDomain.getUserId(), subscriptionDomain.getType(), historyEventType, metaNode));
    }

    /** Общие вспомогательные методы **/
    /**
     * Вспомогательный метод для создания и сохранения записи в историю подписок.
     *
     * @param history Доменная модель `SubscriptionHistory`, содержащая необходимые данные для записи события в историю.
     */
    private void createHistoryEntry(SubscriptionHistory history) {
        subscriptionHistoryRepository.save(subscriptionHistoryPersistenceMapper.toEntity(history));
        log.debug("Сохранена запись в историю: userId={}, type={}, event={}, meta={}",
                history.getUserId(), history.getType(), history.getEventType(), history.getMeta());
    }

    /**
     * Вспомогательный метод для создания метаданных подписки в формате JsonNode.
     *
     * @param message          Сообщение для метаданных.
     * @param paymentId        ID платежа (может быть null).
     * @param subscriptionType Тип подписки (строка).
     * @param confirmationUrl  URL подтверждения (может быть null).
     * @return JsonNode с метаданными.
     */
    private JsonNode createSubscriptionMeta(String message, String paymentId, String subscriptionType, String confirmationUrl) {
        return subscriptionMetaJsonHelper.createSubscriptionMetaNode(message, paymentId, subscriptionType, confirmationUrl);
    }

    /**
     * Вспомогательный метод для определения стоимости подписки. Стоимость пока для примера
     *
     * @param type Тип подписки (`BASIC` или `PRO`).
     * @return Стоимость подписки в виде `double`.
     **/
    private double calculateAmount(SubscriptionType type) {
        if (type == SubscriptionType.BASIC) {
            return 100.00;
        } else {
            return 500.00;
        }
    }

    /** Вспомогательные методы для createSubscription **/

    /**
     * **Вспомогательный метод для инициализации или обновления доменной модели `UserSubscription`.
     * Ищет существующую подписку пользователя; если она найдена (и неактивна), обновляет её,
     * или создает новую `UserSubscription` со статусом `INACTIVE`.
     *
     * @param userId Идентификатор пользователя.
     * @param type   Тип подписки.
     * @return Инициализированная или обновленная доменная модель `UserSubscription`.
     */
    private UserSubscriptionEntity initializeUserSubscriptionEntity(UUID userId, SubscriptionType type) {
        final Optional<UserSubscriptionEntity> existingUserSubscriptionEntity = userSubscriptionRepository.findByUserId(userId);

        UserSubscriptionEntity entity;
        if (existingUserSubscriptionEntity.isPresent()) {
            entity = existingUserSubscriptionEntity.get();
            entity.setType(type);
            entity.setStatus(SubscriptionStatus.INACTIVE);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = userSubscriptionRepository.save(entity);
        } else {
            entity = new UserSubscriptionEntity();
            entity.setUserId(userId);
            entity.setType(type);
            entity.setStatus(SubscriptionStatus.INACTIVE);
            entity.setPaymentProvider(PaymentProvider.YOOKASSA);
            entity.setStartedAt(LocalDateTime.now());
            entity = userSubscriptionRepository.save(entity);
        }
        return entity;
    }

    /**
     * **Вспомогательный метод для логирования начала процесса создания подписки в историю.
     *
     * @param userId         Идентификатор пользователя.
     * @param type           Тип подписки.
     * @param subscriptionId Идентификатор создаваемой/обновляемой подписки.
     */
    private void logSubscriptionInitiation(UUID userId, SubscriptionType type, UUID subscriptionId) {
        final ObjectNode initialMeta = (ObjectNode) subscriptionMetaJsonHelper.createSubscriptionMetaNode(
                "Инициировано создание подписки и платежной сессии.", null, type.name(), null);
        initialMeta.put("subscriptionId", subscriptionId.toString());
        createHistoryEntry(new SubscriptionHistory(userId, type, SubscriptionEventType.INITIATED, initialMeta));
    }

    /**
     * **Вспомогательный метод для взаимодействия с платежным шлюзом и создания платежной сессии.
     *
     * @param userId Идентификатор пользователя.
     * @param type   Тип подписки.
     * @return Ответ от платежного шлюза, содержащий данные платежной сессии.
     * @throws ExecutionException      Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом.
     * @throws InterruptedException    Если текущий поток был прерван во время ожидания ответа от шлюза.
     * @throws PaymentGatewayException Если платежный шлюз возвращает ошибку.
     */
    private PaymentGatewayService.PaymentSessionResponse createPaymentSession(UUID userId, SubscriptionType type)
            throws ExecutionException, InterruptedException {
        final double amount = calculateAmount(type);
        final String description = String.format("%s Подписка для пользователя %s", type.name(), userId.toString());
        try {
            return paymentGatewayService.createPaymentSession(userId, type, amount, description).get();
        } catch (PaymentGatewayException e) {
            log.error("Ошибка при создании платежной сессии со шлюзом для пользователя userId {}: {}", userId, e.getMessage());
            final ObjectNode errorMeta = (ObjectNode) createSubscriptionMeta("Ошибка при создании платежной сессии.", null, type.name(), null);
            errorMeta.put("details", e.getMessage());
            createHistoryEntry(new SubscriptionHistory(userId, type, SubscriptionEventType.FAILED, errorMeta));
            throw e;
        }
    }

    /**
     * **Вспомогательный метод для обновления доменной модели `UserSubscription` данными платежной сессии
     * и сохранения её в репозитории.
     *
     * @param paymentResponse    Ответ от платежного шлюза.
     */
    private void updateAndSaveSubscriptionAfterPaymentSession(UserSubscriptionEntity userSubscriptionEntity,
                                                              PaymentGatewayService.PaymentSessionResponse paymentResponse) {
        userSubscriptionEntity.setExternalPaymentId(paymentResponse.paymentId());
        userSubscriptionEntity.setPaymentSessionUrl(paymentResponse.confirmationUrl());
        userSubscriptionEntity.setQrCodeData(paymentResponse.qrCodeData());
        userSubscriptionEntity.setUpdatedAt(LocalDateTime.now());

        userSubscriptionRepository.save(userSubscriptionEntity);
    }

    /**
     * Вспомогательный метод. Логирует успешное создание платежной сессии в историю и в лог.
     *
     * @param userId          Идентификатор пользователя.
     * @param type            Тип подписки.
     * @param paymentResponse Ответ от платежного шлюза.
     */
    private void logPaymentSessionSuccess(UUID userId, SubscriptionType type, PaymentGatewayService.PaymentSessionResponse paymentResponse) {
        final JsonNode successMeta = createSubscriptionMeta(
                "Платежная сессия успешно создана.", paymentResponse.paymentId(), type.name(), paymentResponse.confirmationUrl());
        createHistoryEntry(new SubscriptionHistory(userId, type, SubscriptionEventType.COMPLETED, successMeta));
        log.info("Подписка создана для пользователя userId: {}, type: {}. Payment URL: {}. Ожидание коллбэка о платеже для активации.",
                userId, type, paymentResponse.confirmationUrl());
    }

    /** Вспомогательные методы для cancelSubscription **/
    /**
     * Вспомогательный метод. Логирует инициацию процесса отмены подписки в историю.
     *
     * @param userId       Идентификатор пользователя.
     * @param subscription Доменная модель `UserSubscription`, которая будет отменена.
     */
    private void logCancelInitiation(UUID userId, UserSubscription subscription) {
        final ObjectNode initialCancelMeta = (ObjectNode) subscriptionMetaJsonHelper.createSubscriptionMetaNode(
                "Инициирована отмена активной подписки.", subscription.getExternalPaymentId(), subscription.getType().name(), null);
        initialCancelMeta.put("subscriptionId", subscription.getId().toString());
        createHistoryEntry(new SubscriptionHistory(userId, subscription.getType(), SubscriptionEventType.INITIATED, initialCancelMeta));
    }

    /**
     * Вспомогательный метод. Обрабатывает попытку отмены платежа через платежный шлюз.
     * Если `externalPaymentId` существует, пытается отменить платеж. Если шлюз не подтверждает
     * отмену, логирует предупреждение.
     *
     * @param userId       Идентификатор пользователя.
     * @param subscription Доменная модель `UserSubscription`.
     * @throws ExecutionException   Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом.
     * @throws InterruptedException Если текущий поток был прерван во время ожидания ответа от шлюза.
     */
    private void handleGatewayCancellation(UUID userId, UserSubscription subscription) throws ExecutionException, InterruptedException {
        if (subscription.getExternalPaymentId() != null) {
            final boolean cancelledInGateway = paymentGatewayService.cancelPayment(subscription.getExternalPaymentId()).get();
            if (!cancelledInGateway) {
                log.warn("Платежный шлюз не подтвердил отмену платежа для подписки ID: {}", subscription.getId());
                final JsonNode gatewayWarningMeta = createSubscriptionMeta(
                        "Платежный шлюз не подтвердил отмену, но подписка в нашей системе будет помечена как отмененная.",
                        subscription.getExternalPaymentId(), subscription.getType().name(), null);
                createHistoryEntry(new SubscriptionHistory(userId, subscription.getType(), SubscriptionEventType.FAILED, gatewayWarningMeta));
            }
        } else {
            log.info("Отмена подписки userId: {} без внешнего платежного ID (не требовалась отмена в шлюзе).", userId);
        }
    }

    /**
     * Вспомогательный метод. Завершает процесс отмены подписки в локальной системе.
     * Вызывает метод `cancel()` на доменной модели `UserSubscription` и сохраняет измененную сущность.
     * Записывает успешное завершение отмены в историю.
     *
     * @param userId       Идентификатор пользователя.
     */
    private void finalizeSubscriptionCancellation(UUID userId, UserSubscriptionEntity userSubscriptionEntityToCancel) {

        final UserSubscription subscriptionDomain = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntityToCancel);
        subscriptionDomain.cancel();
        userSubscriptionEntityToCancel.setStatus(subscriptionDomain.getStatus());
        userSubscriptionEntityToCancel.setExpiresAt(LocalDateTime.now());

        userSubscriptionRepository.save(userSubscriptionEntityToCancel);

        final JsonNode successCancelMeta = createSubscriptionMeta(
                "Подписка успешно отменена пользователем.", subscriptionDomain.getExternalPaymentId(), subscriptionDomain.getType().name(), null);
        createHistoryEntry(new SubscriptionHistory(userId, subscriptionDomain.getType(), SubscriptionEventType.COMPLETED, successCancelMeta));
    }

    /**
     * Вспомогательный метод. Логирует неудачу отмены подписки из-за ошибки платежного шлюза
     * и обновляет статус подписки в системе на `INACTIVE`.
     * Это обеспечивает, что даже при ошибках во внешней системе, состояние подписки в БД остается.
     *
     * @param userId       Идентификатор пользователя.

     * @param e            Исключение `PaymentGatewayException`, вызванное ошибкой.
     */
    private void logCancellationFailure(UUID userId, UserSubscriptionEntity userSubscriptionEntityToCancel, PaymentGatewayException e) {
        final JsonNode errorCancelMeta = createSubscriptionMeta(
                "Ошибка при отмене платежа в платежном шлюзе.", userSubscriptionEntityToCancel.getExternalPaymentId(),
                userSubscriptionEntityToCancel.getType().name(), null);
        ((ObjectNode) errorCancelMeta).put("details", e.getMessage());
        createHistoryEntry(new SubscriptionHistory(userId, userSubscriptionEntityToCancel.getType(), SubscriptionEventType.FAILED, errorCancelMeta));

        final UserSubscription subscriptionDomain = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntityToCancel);
        subscriptionDomain.cancel();
        userSubscriptionEntityToCancel.setStatus(subscriptionDomain.getStatus());
        userSubscriptionEntityToCancel.setExpiresAt(LocalDateTime.now());

        userSubscriptionRepository.save(userSubscriptionEntityToCancel);
    }

}
