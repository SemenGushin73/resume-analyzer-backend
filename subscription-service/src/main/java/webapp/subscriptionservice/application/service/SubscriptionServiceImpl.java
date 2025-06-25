package webapp.subscriptionservice.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

import static webapp.subscriptionservice.application.util.LogSanitizer.sanitizeLogInput;


/**
 * Основной класс с бизнес-логикой.
 * Реализует интерфейс {@link SubscriptionService}, предоставляет методы для создания, отмены,
 * получения истории и активной подписки, а также обработки коллбэков от платежных шлюзов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final SubscriptionMapper subscriptionMapper;
    @SuppressFBWarnings("EI2")
    private final ObjectMapper objectMapper;
    private final UserSubscriptionMapper userSubscriptionPersistenceMapper;
    private final SubscriptionHistoryMapper subscriptionHistoryPersistenceMapper;
    private final SubscriptionMetaJsonHelper subscriptionMetaJsonHelper;

    /**
     * Создает новую подписку для пользователя и инициирует платежную сессию. Шаги:
     * Проверяет, нет ли у пользователя уже активной подписки.
     * Инициализирует (создает или обновляет) сущность UserSubscriptionEntity со статусом INACTIVE.
     * Записывает событие инициации создания подписки в историю.
     * Взаимодействует с PaymentGatewayService для создания платежной сессии.
     * Обновляет сущность подписки данными, полученными от платежного шлюза (ID платежа, URL, QR-код) и сохраняет её.
     * Записывает успешное завершение создания платежной сессии в историю.
     *
     * @param userId Идентификатор пользователя, для которого создается подписка.
     * @param type   Тип подписки
     * @return DTO PaymentSessionResponseDto с информацией о созданной платежной сессии (ID платежа, URL подтверждения, QR-код).
     * @throws IllegalArgumentException Если у пользователя уже есть активная подписка.
     * @throws ExecutionException       Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом
     * @throws InterruptedException     Если текущий поток был прерван во время ожидания ответа от шлюза.
     * @throws PaymentGatewayException  Если платежный шлюз возвращает ошибку (транслируется из PaymentGatewayService).
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
     * Отменяет активную подписку пользователя. Шаги:
     * Находит активную подписку пользователя по userId.
     * Записывает событие инициации отмены в историю подписок.
     * Пытается отменить платеж через платежный шлюз, если externalPaymentId существует.
     * Обновляет статус подписки на INACTIVE (используя доменную логику cancel()) и сохраняет её в репозитории.
     * Записывает результат отмены (успех или предупреждение/ошибка) в историю подписок.
     * В случае ошибки отмены платежа через шлюз, логирует ошибку, но все равно переводит подписку в статус INACTIVE.
     *
     * @param userId Идентификатор пользователя, чья подписка должна быть отменена.
     * @throws ResourceNotFoundException Если активная подписка для указанного пользователя не найдена.
     * @throws ExecutionException        Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом
     *                                   во время отмены платежа.
     * @throws InterruptedException      Если текущий поток был прерван во время ожидания ответа от шлюза.
     * @throws PaymentGatewayException   Если платежный шлюз возвращает ошибку во время отмены (транслируется из @code PaymentGatewayService).
     */
    @Transactional
    @Override
    public void cancelSubscription(UUID userId) throws ExecutionException, InterruptedException {

        final UserSubscriptionEntity userSubscriptionEntityToCancel = userSubscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Активная подписка не найдена: " + sanitizeLogInput(userId.toString())));

        final UserSubscription domainSubscriptionToCancel = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntityToCancel);

        logCancelInitiation(userId, domainSubscriptionToCancel);

        try {
            handleGatewayCancellation(userId, domainSubscriptionToCancel);

            finalizeSubscriptionCancellation(userId, userSubscriptionEntityToCancel);
            log.info("Отменена подписка для пользователя userId: {}", sanitizeLogInput(userId.toString()));
        } catch (PaymentGatewayException e) {
            log.error("Ошибка отмены платежа в шлюзе для пользователя userId {}: {}",
                    sanitizeLogInput(userId.toString()), sanitizeLogInput(e.getMessage()));
            logCancellationFailure(userId, userSubscriptionEntityToCancel, e);
            throw e;
        }
    }

    /**
     * Получает историю подписок для указанного пользователя.
     * Возвращает список всех записей истории подписок для пользователя,
     * Сущности из репозитория преобразуются сначала в доменные модели, а затем в DTO перед возвратом
     *
     * @param userId Идентификатор пользователя.
     * @return Список DTO SubscriptionHistoryDto, представляющих историю подписок пользователя,
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
     * Получает активную подписку для указанного пользователя.
     * Метод возвращает информацию о текущей активной подписке пользователя, если она имеется.
     * Сущность из репозитория преобразуется сначала в доменную модель, а затем в DTO перед возвратом.
     *
     * @param userId Идентификатор пользователя.
     * @return Optional с UserSubscriptionDto, представляющим активную подписку пользователя.
     * Если активная подписка не найдена, возвращается пустой Optional.
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<UserSubscriptionDto> getActiveSubscription(UUID userId) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(userSubscriptionPersistenceMapper::toDomainModel)
                .map(subscriptionMapper::toUserSubscriptionDto);
    }

    /**
     * Обрабатывает коллбэк от платежного шлюза о статусе платежа (успех, неудача и т.д.). Шаги:
     * Находит соответствующую подписку по paymentId. Если подписка не найдена, логирует ошибку и выбрасывает исключение.
     * Парсит и подготавливает метаданные коллбэка для записи в историю.
     * На основе полученного статуса платежа из шлюза:
     * Если статус SUCCEEDED: Активирует подписку (используя доменную логику activate()),
     * устанавливает дату истечения и логирует успешное завершение.
     * Если статус FAILED: Отменяет подписку (используя доменную логику cancel()), логирует неудачу.
     * В случае другого статуса (default): Устанавливает подписку в статус INACTIVE и логирует неизвестный/необрабатываемый статус.
     * Сохраняет обновленную сущность подписки.
     * Создает запись в истории подписок о результате коллбэка.
     *
     * @param paymentId    Идентификатор платежа во внешней системе (платежном шлюзе).
     * @param status       Статус платежа, полученный от платежного шлюза (например, {@code SUCCEEDED}, {@code FAILED}).
     * @param metadataJson JSON-строка, содержащая дополнительные метаданные от платежного шлюза.
     * @throws ResourceNotFoundException Если подписка с указанным paymentId не найдена в системе.
     */
    @Transactional
    @Override
    public void handlePaymentCallback(String paymentId, PaymentStatus status, String metadataJson) {
        log.info("Обработка коллбэка о платеже для paymentId: {}, status: {}",
                sanitizeLogInput(paymentId), sanitizeLogInput(status.name()));

        final UserSubscriptionEntity userSubscriptionEntity = userSubscriptionRepository.findByExternalPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.error("Не найдена подписка для платежа payment ID: {}", sanitizeLogInput(paymentId));
                    return new ResourceNotFoundException("Не найдена подписка для платежа payment ID: " + sanitizeLogInput(paymentId));
                });

        final UserSubscription subscriptionDomain = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntity);

        final ObjectNode metaNode = subscriptionMetaJsonHelper.prepareCallbackMetaNode(
                sanitizeLogInput(paymentId), sanitizeLogInput(status.name()), sanitizeLogInput(metadataJson), subscriptionDomain.getId()
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

        log.info("{} Пользователь: {}, Подписка: {}",
                sanitizeLogInput(logMessage),
                sanitizeLogInput(subscriptionDomain.getUserId().toString()),
                sanitizeLogInput(subscriptionDomain.getType().name()));

        userSubscriptionEntity.setStatus(subscriptionDomain.getStatus());
        userSubscriptionEntity.setExpiresAt(subscriptionDomain.getExpiresAt());
        userSubscriptionEntity.setStartedAt(subscriptionDomain.getStartedAt());

        userSubscriptionRepository.save(userSubscriptionEntity);

        createHistoryEntry(new SubscriptionHistory(subscriptionDomain.getUserId(), subscriptionDomain.getType(), historyEventType, metaNode));
    }

    /** Общие вспомогательные методы **/

    /**
     * Вспомогательный метод для создания и сохранения записи в историю подписок.
     * Преобразует доменную модель истории в сущность и сохраняет её.
     *
     * @param history Доменная модель SubscriptionHistory, содержащая необходимые данные для записи события в историю.
     */
    private void createHistoryEntry(SubscriptionHistory history) {
        subscriptionHistoryRepository.save(subscriptionHistoryPersistenceMapper.toEntity(history));
        log.debug("Сохранена запись в историю: userId={}, type={}, event={}, meta={}",
                sanitizeLogInput(history.getUserId().toString()),
                sanitizeLogInput(history.getType().name()),
                sanitizeLogInput(history.getEventType().name()),
                sanitizeLogInput(history.getMeta().toString()));
    }

    /**
     * Вспомогательный метод для создания метаданных подписки в формате JsonNode.
     * Этот метод делегирует создание метаданных SubscriptionMetaJsonHelper,
     * обеспечивая санитаризацию всех строковых входов.
     *
     * @param message          Сообщение для метаданных.
     * @param paymentId        ID платежа (может быть null).
     * @param subscriptionType Тип подписки (строка, например, "BASIC").
     * @param confirmationUrl  URL подтверждения (может быть null).
     * @return {@link JsonNode} с метаданными.
     */
    private JsonNode createSubscriptionMeta(String message, String paymentId, String subscriptionType, String confirmationUrl) {
        return subscriptionMetaJsonHelper.createSubscriptionMetaNode(
                sanitizeLogInput(message),
                paymentId != null ? sanitizeLogInput(paymentId) : null,
                sanitizeLogInput(subscriptionType),
                confirmationUrl != null ? sanitizeLogInput(confirmationUrl) : null
        );
    }

    /**
     * Вспомогательный метод для определения стоимости подписки в зависимости от её типа.
     * Стоимость пока является примером и может быть изменена в дальнейшем.</p>
     *
     * @param type Тип подписки (BASIC или PRO).
     * @return Стоимость подписки в виде double.
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
     * Вспомогательный метод для инициализации или обновления сущности UserSubscriptionEntity.
     * Ищет существующую подписку пользователя по userId. Если она найдена (независимо от статуса),
     * обновляет её тип и статус на INACTIVE, а также updatedAt.
     * Если подписка не найдена, создает новую сущность со статусом INACTIVE и провайдером платежей.
     *
     * @param userId Идентификатор пользователя.
     * @param type   Тип подписки.
     * @return Инициализированная или обновленная сущность UserSubscriptionEntity.
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
     * Вспомогательный метод для логирования начала процесса создания подписки в историю.
     * Создает запись в истории подписок с типом события INITIATED и метаданными,
     * указывающими на инициацию создания подписки и платежной сессии.
     *
     * @param userId         Идентификатор пользователя.
     * @param type           Тип подписки.
     * @param subscriptionId Идентификатор создаваемой/обновляемой подписки.
     */
    private void logSubscriptionInitiation(UUID userId, SubscriptionType type, UUID subscriptionId) {
        final ObjectNode initialMeta = (ObjectNode) subscriptionMetaJsonHelper.createSubscriptionMetaNode(
                sanitizeLogInput("Инициировано создание подписки и платежной сессии."), null, sanitizeLogInput(type.name()), null);
        initialMeta.put("subscriptionId", sanitizeLogInput(subscriptionId.toString()));
        createHistoryEntry(new SubscriptionHistory(userId, type, SubscriptionEventType.INITIATED, initialMeta));
    }

    /**
     * Вспомогательный метод для взаимодействия с платежным шлюзом и создания платежной сессии.
     * Вычисляет сумму на основе типа подписки и формирует описание платежа.
     * Делегирует вызов paymentGatewayService и обрабатывает возможные исключения от шлюза,
     * логируя ошибки и создавая соответствующую запись в истории подписок.
     *
     * @param userId Идентификатор пользователя.
     * @param type   Тип подписки.
     * @return Объект PaymentSessionResponse, содержащий данные платежной сессии.
     * @throws ExecutionException      Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом.
     * @throws InterruptedException    Если текущий поток был прерван во время ожидания ответа от шлюза.
     * @throws PaymentGatewayException Если платежный шлюз возвращает ошибку (перебрасывается).
     */
    private PaymentGatewayService.PaymentSessionResponse createPaymentSession(UUID userId, SubscriptionType type)
            throws ExecutionException, InterruptedException {
        final double amount = calculateAmount(type);
        final String description = String.format("%s Подписка для пользователя %s", sanitizeLogInput(type.name()), sanitizeLogInput(userId.toString()));
        try {
            return paymentGatewayService.createPaymentSession(userId, type, amount, description).get();
        } catch (PaymentGatewayException e) {
            log.error("Ошибка при создании платежной сессии со шлюзом для пользователя userId {}: {}",
                    sanitizeLogInput(userId.toString()), sanitizeLogInput(e.getMessage()));
            final ObjectNode errorMeta = (ObjectNode) createSubscriptionMeta("Ошибка при создании платежной сессии.", null, type.name(), null);
            errorMeta.put("details", sanitizeLogInput(e.getMessage()));
            createHistoryEntry(new SubscriptionHistory(userId, type, SubscriptionEventType.FAILED, errorMeta));
            throw e;
        }
    }

    /**
     * Вспомогательный метод для обновления сущности UserSubscriptionEntity данными платежной сессии и сохранения её в репозитории.
     * Устанавливает externalPaymentId, paymentSessionUrl, qrCodeData и updatedAt в сущности, а затем сохраняет её.
     *
     * @param userSubscriptionEntity Сущность UserSubscriptionEntity, которая будет обновлена.
     * @param paymentResponse        Ответ от платежного шлюза, содержащий данные платежной сессии.
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
     * Вспомогательный метод. Логирует успешное создание платежной сессии в историю подписок и в лог.
     * Создает запись в истории с типом события COMPLETED и метаданными о созданной сессии,
     * а также выводит информационное сообщение в лог.
     *
     * @param userId          Идентификатор пользователя.
     * @param type            Тип подписки.
     * @param paymentResponse Ответ от платежного шлюза, содержащий данные платежной сессии.
     */
    private void logPaymentSessionSuccess(UUID userId, SubscriptionType type, PaymentGatewayService.PaymentSessionResponse paymentResponse) {
        final JsonNode successMeta = createSubscriptionMeta(
                "Платежная сессия успешно создана.", paymentResponse.paymentId(), type.name(), paymentResponse.confirmationUrl());
        createHistoryEntry(new SubscriptionHistory(userId, type, SubscriptionEventType.COMPLETED, successMeta));
        log.info("Подписка создана для пользователя userId: {}, type: {}. Payment URL: {}. Ожидание коллбэка о платеже для активации.",
                sanitizeLogInput(userId.toString()),
                sanitizeLogInput(type.name()),
                sanitizeLogInput(paymentResponse.confirmationUrl()));
    }

    /** Вспомогательные методы для cancelSubscription **/

    /**
     * Вспомогательный метод. Логирует инициацию процесса отмены подписки в историю.
     * Создает запись в истории подписок с типом события INITIATED и метаданными,
     * указывающими на начало процесса отмены активной подписки.
     *
     * @param userId       Идентификатор пользователя.
     * @param subscription Доменная модель UserSubscription, которая будет отменена.
     */
    private void logCancelInitiation(UUID userId, UserSubscription subscription) {
        final ObjectNode initialCancelMeta = (ObjectNode) subscriptionMetaJsonHelper.createSubscriptionMetaNode(
                sanitizeLogInput("Инициирована отмена активной подписки."),
                subscription.getExternalPaymentId() != null ? sanitizeLogInput(subscription.getExternalPaymentId()) : null,
                sanitizeLogInput(subscription.getType().name()), null);
        initialCancelMeta.put("subscriptionId", sanitizeLogInput(subscription.getId().toString()));
        createHistoryEntry(new SubscriptionHistory(userId, subscription.getType(), SubscriptionEventType.INITIATED, initialCancelMeta));
    }

    /**
     * Вспомогательный метод. Обрабатывает попытку отмены платежа через платежный шлюз.
     * Если у подписки есть externalPaymentId, метод пытается отменить соответствующий платеж
     * через PaymentGatewayService. Если шлюз не подтверждает отмену, логируется предупреждение,
     * и создается соответствующая запись в истории подписок.
     *
     * @param userId       Идентификатор пользователя.
     * @param subscription Доменная модель UserSubscription, для которой производится отмена.
     * @throws ExecutionException   Если возникает ошибка при асинхронном взаимодействии с платежным шлюзом.
     * @throws InterruptedException Если текущий поток был прерван во время ожидания ответа от шлюза.
     */
    private void handleGatewayCancellation(UUID userId, UserSubscription subscription) throws ExecutionException, InterruptedException {
        if (subscription.getExternalPaymentId() != null) {
            final boolean cancelledInGateway = paymentGatewayService.cancelPayment(subscription.getExternalPaymentId()).get();
            if (!cancelledInGateway) {
                log.warn("Платежный шлюз не подтвердил отмену платежа для подписки ID: {}", sanitizeLogInput(subscription.getId().toString()));
                final JsonNode gatewayWarningMeta = createSubscriptionMeta(
                        "Платежный шлюз не подтвердил отмену, но подписка в нашей системе будет помечена как отмененная.",
                        subscription.getExternalPaymentId(), subscription.getType().name(), null);
                createHistoryEntry(new SubscriptionHistory(userId, subscription.getType(), SubscriptionEventType.FAILED, gatewayWarningMeta));
            }
        } else {
            log.info("Отмена подписки userId: {} без внешнего платежного ID (не требовалась отмена в шлюзе).", sanitizeLogInput(userId.toString()));
        }
    }

    /**
     * Вспомогательный метод. Завершает процесс отмены подписки в локальной системе.
     * Метод cancel() на доменной модели UserSubscription для изменения статуса
     * на INACTIVE и установки даты истечения на текущий момент. Затем сохраняет измененную сущность.
     * Создает запись в истории подписок о завершении отмены.
     *
     * @param userId                         Идентификатор пользователя.
     * @param userSubscriptionEntityToCancel Сущность UserSubscriptionEntity, представляющая отменяемую подписку.
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
     * Вспомогательный метод. Логирует неудачу отмены подписки, вызванную ошибкой платежного шлюза.
     * Обновляет статус подписки в локальной системе на INACTIVE, даже если отмена во внешней системе не удалась.
     * Это обеспечивает консистентность состояния в локальной базе данных.
     * Создает соответствующую запись в истории подписок с типом события FAILED}.
     *
     * @param userId                         Идентификатор пользователя.
     * @param userSubscriptionEntityToCancel Сущность UserSubscriptionEntity, представляющая отменяемую подписку.
     * @param e                              Исключение PaymentGatewayException, вызванное ошибкой шлюза.
     */
    private void logCancellationFailure(UUID userId, UserSubscriptionEntity userSubscriptionEntityToCancel, PaymentGatewayException e) {
        final JsonNode errorCancelMeta = createSubscriptionMeta(
                "Ошибка при отмене платежа в платежном шлюзе.", userSubscriptionEntityToCancel.getExternalPaymentId(),
                userSubscriptionEntityToCancel.getType().name(), null);
        ((ObjectNode) errorCancelMeta).put("details", sanitizeLogInput(e.getMessage()));
        createHistoryEntry(new SubscriptionHistory(userId, userSubscriptionEntityToCancel.getType(), SubscriptionEventType.FAILED, errorCancelMeta));

        final UserSubscription subscriptionDomain = userSubscriptionPersistenceMapper.toDomainModel(userSubscriptionEntityToCancel);
        subscriptionDomain.cancel();
        userSubscriptionEntityToCancel.setStatus(subscriptionDomain.getStatus());
        userSubscriptionEntityToCancel.setExpiresAt(LocalDateTime.now());

        userSubscriptionRepository.save(userSubscriptionEntityToCancel);
    }

}
