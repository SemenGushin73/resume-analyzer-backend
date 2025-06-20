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

import ru.kata.project.model.PaymentProvider;
import ru.kata.project.model.SubscriptionEventType;
import ru.kata.project.model.SubscriptionStatus;
import ru.kata.project.model.SubscriptionType;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.SubscriptionMetaDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;
import webapp.subscriptionservice.application.mapper.SubscriptionMapper;

import webapp.subscriptionservice.domain.model.*;
import webapp.subscriptionservice.domain.repository.SubscriptionHistoryRepository;
import webapp.subscriptionservice.domain.repository.UserSubscriptionRepository;
import webapp.subscriptionservice.domain.service.PaymentGatewayService;
import webapp.subscriptionservice.domain.service.SubscriptionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * Основной класс с бизнес-логикой, связанной с управлением подписками пользователей. Взаимодействует:
 * с репозиториями для сохранения и получения данных,
 * с платежным шлюзом для обработки платежей,
 * с маппером для преобразования данных.
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

    /**
     * Вспомогательный метод для создания и сохранения записи в историю подписок.
     * Для избежания дублирования кода при логировании событий.
     *
     * @param userId           UUID пользователя.
     * @param subscriptionType Тип подписки, к которой относится событие.
     * @param eventType        Тип события (INITIATED, COMPLETED, FAILED).
     * @param meta             Метаданные события в формате JsonNode.
     */
    private void createHistoryEntry(UUID userId, SubscriptionType subscriptionType, SubscriptionEventType eventType, JsonNode meta) {
        final SubscriptionHistory historyEntry = new SubscriptionHistory(
                userId,
                subscriptionType,
                eventType,
                meta,
                LocalDateTime.now()
        );
        subscriptionHistoryRepository.save(historyEntry);
        log.debug("Сохранена запись в историю: userId={}, type={}, event={}, meta={}", userId, subscriptionType, eventType, meta);
    }

    /**
     * Вспомогательный метод для создания метаданных подписки в формате JsonNode.
     *
     * @param message          Сообщение для метаданных.
     * @param paymentId        ID платежа (может быть null).
     * @param subscriptionType Тип подписки (строковое представление).
     * @param confirmationUrl  URL подтверждения (может быть null).
     * @return JsonNode с метаданными.
     */
    private JsonNode createSubscriptionMeta(String message, String paymentId, String subscriptionType, String confirmationUrl) {
        final SubscriptionMetaDto metaDto = new SubscriptionMetaDto();
        metaDto.setMessage(message);
        metaDto.setPaymentId(paymentId);
        metaDto.setSubscriptionType(subscriptionType);
        metaDto.setConfirmationUrl(confirmationUrl);
        return objectMapper.valueToTree(metaDto);
    }

    /**
     * Вспомогательный метод для настройки новой или существующей подписки.
     *
     * @param userSubscription Объект UserSubscription для настройки.
     * @param userId           UUID пользователя.
     * @param type             Тип подписки.
     * @param status           Статус подписки.
     */
    private void setupNewSubscription(UserSubscription userSubscription, UUID userId, SubscriptionType type, SubscriptionStatus status) {
        userSubscription.setUserId(userId);
        userSubscription.setType(type);
        userSubscription.setStatus(status);
        userSubscription.setCreatedAt(LocalDateTime.now());
        userSubscription.setUpdatedAt(LocalDateTime.now());
        userSubscriptionRepository.save(userSubscription);
    }


    /**
     * Метод для создания новой подписки.
     * Проверяет, нет ли у пользователя уже активной подписки.
     * Инициирует создание подписки, сохраняя её начальный статус как INACTIVE и логируя это событие.
     * Создает платежную сессию через платежный шлюз.
     * В случае успешного создания платежной сессии, обновляет подписку и логирует событие завершения.
     * При возникновении ошибок в процессе взаимодействия со шлюзом, логирует ошибку и выбрасывает исключение.
     *
     * @param userId Идентификатор пользователя.
     * @param type   Тип подписки (BASIC, PRO).
     * @return DTO с информацией о платежной сессии (ID платежа, URL подтверждения, QR-код).
     * @throws IllegalArgumentException если у пользователя уже есть активная подписка.
     * @throws ExecutionException       если возникает ошибка при взаимодействии с платежным шлюзом.
     * @throws InterruptedException     если текущий поток был прерван во время ожидания ответа от шлюза.
     */
    @Transactional
    @Override
    public PaymentSessionResponseDto createSubscription(UUID userId, SubscriptionType type)
            throws ExecutionException, InterruptedException {
        final Optional<UserSubscription> existingSubscription = userSubscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        if (existingSubscription.isPresent()) {
            throw new IllegalArgumentException("У пользователя уже есть активная подписка");
        }

        /** Создаем и сохраняем новую подписку с начальным статусом INACTIVE, используя вспомогательный метод. **/
        final UserSubscription subscription = userSubscriptionRepository.findByUserId(userId)
                .orElse(new UserSubscription());
        setupNewSubscription(subscription, userId, type, SubscriptionStatus.INACTIVE);
        subscription.setPaymentProvider(PaymentProvider.YOOKASSA);

        /** Логируем намерение создать подписку в историю, используя вспомогательный метод. **/
        final JsonNode initialMeta = createSubscriptionMeta(
                "Инициировано создание подписки и платежной сессии.",
                null, type.name(), null);
        ((ObjectNode) initialMeta).put("subscriptionId", subscription.getId().toString());
        createHistoryEntry(userId, type, SubscriptionEventType.INITIATED, initialMeta);

        final double amount;
        if (type == SubscriptionType.BASIC) {
            amount = 100.00;
        } else {
            amount = 500.00;
        }
        final String description = String.format("%s Подписка для пользователя %s", type.name(), userId.toString());

        try {
            /** Вызываем платежный шлюз для создания сессии. **/
            final PaymentGatewayService.PaymentSessionResponse paymentResponse =
                    paymentGatewayService.createPaymentSession(userId, type, amount, description).get();

            /** Обновляем подписку данными из платежной сессии. **/
            subscription.setExternalPaymentId(paymentResponse.paymentId());
            subscription.setPaymentSessionUrl(paymentResponse.confirmationUrl());
            subscription.setQrCodeData(paymentResponse.qrCodeData());
            userSubscriptionRepository.save(subscription);

            /** Логируем успешное создание платежной сессии в историю, используя вспомогательный метод. **/
            final JsonNode successMeta = createSubscriptionMeta(
                    "Платежная сессия успешно создана.",
                    paymentResponse.paymentId(), type.name(), paymentResponse.confirmationUrl());
            createHistoryEntry(userId, type, SubscriptionEventType.COMPLETED, successMeta);

            log.info("Подписка создана для пользователя userId: {}, type: {}. "
                            + "Payment URL: {}. Ожидание коллбэка о платеже для активации.",
                    userId, type, paymentResponse.confirmationUrl());
            return new PaymentSessionResponseDto(paymentResponse.paymentId(),
                    paymentResponse.confirmationUrl(), paymentResponse.qrCodeData());

        } catch (PaymentGatewayException e) {
            log.error("Ошибка при создании платежной сессии со шлюзом для пользователя userId {}: {}",
                    userId, e.getMessage());

            /** Логируем ошибку создания платежной сессии в историю, используя вспомогательный метод.
             * Подписка остается в статусе INACTIVE **/
            final JsonNode errorMeta = createSubscriptionMeta(
                    "Ошибка при создании платежной сессии.",
                    null, type.name(), null);
            ((ObjectNode) errorMeta).put("details", e.getMessage());
            createHistoryEntry(userId, type, SubscriptionEventType.FAILED, errorMeta);
            throw e;
        }
    }

    /**
     * Метод для отмены подписки.
     * Ищет активную подписку пользователя.
     * Инициирует процесс отмены, записывая событие в историю.
     * Пытается отменить соответствующий платеж через платежный шлюз (если применимая логика).
     * Обновляет статус подписки на INACTIVE и логирует результат отмены в историю.
     *
     * @param userId Идентификатор пользователя, чья подписка должна быть отменена.
     * @throws ResourceNotFoundException если активная подписка для указанного пользователя не найдена.
     * @throws ExecutionException        если возникает ошибка при взаимодействии с платежным шлюзом.
     * @throws InterruptedException      если текущий поток был прерван во время ожидания ответа от шлюза.
     */
    @Transactional
    @Override
    public void cancelSubscription(UUID userId) throws ExecutionException, InterruptedException {
        final UserSubscription activeSubscription = userSubscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Активная подписка не найдена: " + userId));

        /** Логируем намерение отменить подписку в историю, используя вспомогательный метод. **/
        final JsonNode initialCancelMeta = createSubscriptionMeta(
                "Инициирована отмена активной подписки.",
                activeSubscription.getExternalPaymentId(), activeSubscription.getType().name(), null);
        ((ObjectNode) initialCancelMeta).put("subscriptionId", activeSubscription.getId().toString());
        createHistoryEntry(userId, activeSubscription.getType(),
                SubscriptionEventType.INITIATED, initialCancelMeta);

        try {
            /** Вызываем платежный шлюз для отмены платежа (если externalPaymentId существует). **/
            if (activeSubscription.getExternalPaymentId() != null) {
                final boolean cancelledInGateway = paymentGatewayService
                        .cancelPayment(activeSubscription.getExternalPaymentId()).get();
                if (!cancelledInGateway) {
                    log.warn("Платежный шлюз не подтвердил отмену платежа для подписки ID: {}",
                            activeSubscription.getId());
                    final JsonNode gatewayWarningMeta = createSubscriptionMeta(
                            "Платежный шлюз не подтвердил отмену, но подписка в нашей системе будет помечена как неактивная.",
                            activeSubscription.getExternalPaymentId(), activeSubscription.getType().name(), null);
                    createHistoryEntry(userId, activeSubscription.getType(),
                            SubscriptionEventType.FAILED, gatewayWarningMeta);
                }
            } else {
                log.info("Отмена подписки userId: {} без внешнего платежного ID "
                        + "(не требовалась отмена в шлюзе).", userId);
            }

            /** Обновляем статус подписки в нашей системе на INACTIVE. **/
            activeSubscription.setStatus(SubscriptionStatus.INACTIVE);
            activeSubscription.setExpiresAt(LocalDateTime.now());
            activeSubscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(activeSubscription);

            /** Логируем успешное завершение отмены подписки в историю, используя вспомогательный метод. **/
            final JsonNode successCancelMeta = createSubscriptionMeta(
                    "Подписка успешно отменена пользователем.",
                    activeSubscription.getExternalPaymentId(), activeSubscription.getType().name(), null);
            createHistoryEntry(userId, activeSubscription.getType(),
                    SubscriptionEventType.COMPLETED, successCancelMeta);

            log.info("Отменена подписка для пользователя userId: {}", userId);

        } catch (PaymentGatewayException e) {
            log.error("Ошибка отмены платежа в шлюзе для пользователя userId {}: {}", userId, e.getMessage());

            /** Логируем ошибку отмены платежа в историю, используя вспомогательный метод. **/
            final JsonNode errorCancelMeta = createSubscriptionMeta(
                    "Ошибка при отмене платежа в платежном шлюзе.",
                    activeSubscription.getExternalPaymentId(), activeSubscription.getType().name(), null);
            ((ObjectNode) errorCancelMeta).put("details", e.getMessage());
            createHistoryEntry(userId, activeSubscription.getType(),
                    SubscriptionEventType.FAILED, errorCancelMeta);

            /** Даже при ошибке в шлюзе, подписка в системе переводится в INACTIVE **/
            activeSubscription.setStatus(SubscriptionStatus.INACTIVE);
            activeSubscription.setExpiresAt(LocalDateTime.now());
            activeSubscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(activeSubscription);

            throw e;
        }
    }

    /**
     * Метод для получения истории подписок пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список DTO истории подписок.
     */
    @Transactional(readOnly = true)
    @Override
    public List<SubscriptionHistoryDto> getSubscriptionHistory(UUID userId) {
        final List<SubscriptionHistory> history = subscriptionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return history.stream()
                .map(subscriptionMapper::toHistoryDto)
                .toList();
    }

    /**
     * Метод для получения информации об активной подписке пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Опциональный DTO активной подписки пользователя.
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<UserSubscriptionDto> getActiveSubscription(UUID userId) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(subscriptionMapper::toUserSubscriptionDto);
    }

    /**
     * Метод для обработки коллбэков от платежного шлюза о статусе платежа.
     * Находит соответствующую подписку, обновляет ее статус и создает запись в истории.
     *
     * @param paymentId    Идентификатор платежа из коллбэка.
     * @param status       Статус платежа ("succeeded", "failed" и т.д.).
     * @param metadataJson Дополнительные метаданные от платежного шлюза в формате JSON.
     * @throws ResourceNotFoundException если подписка с указанным paymentId не найдена.
     */
    @Transactional
    @Override
    public void handlePaymentCallback(String paymentId, String status, String metadataJson) {
        log.info("Обработка коллбэка о платеже для paymentId: {}, status: {}", paymentId, status);

        final UserSubscription subscription = userSubscriptionRepository.findByExternalPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.error("Не найдена подписка для платежа payment ID: {}", paymentId);
                    return new ResourceNotFoundException(
                            "Не найдена подписка для платежа payment ID: " + paymentId);
                });

        final ObjectNode metaNode = objectMapper.createObjectNode();
        metaNode.put("paymentId", paymentId);
        metaNode.put("statusFromGateway", status);

        if (metadataJson != null && !metadataJson.isEmpty()) {
            try {
                final JsonNode externalMeta = objectMapper.readTree(metadataJson);
                metaNode.set("externalData", externalMeta);
            } catch (JsonProcessingException e) {
                metaNode.put("parseError", "Ошибка парсинга metadata");
                metaNode.put("rawData", metadataJson);
                log.warn("Ошибка парсинга метаданных коллбэка для paymentId {}: {}",
                        paymentId, e.getMessage());
            }
        }

        final SubscriptionEventType historyEventType;
        final String logMessage;

        switch (status) {
            case "succeeded":
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setExpiresAt(LocalDateTime.now().plusMonths(1));
                historyEventType = SubscriptionEventType.COMPLETED;
                logMessage = "Оплата прошла успешно. Подписка активирована.";
                metaNode.put("action", "Активация подписки");
                break;

            case "failed":
                subscription.setStatus(SubscriptionStatus.INACTIVE);
                subscription.setExpiresAt(LocalDateTime.now());
                historyEventType = SubscriptionEventType.FAILED;
                logMessage = "Ошибка или отмена платежа. Подписка помечена как неактивная.";
                metaNode.put("action", "Автоматическая деактивация");
                break;

            default:
                subscription.setStatus(SubscriptionStatus.INACTIVE);
                historyEventType = SubscriptionEventType.FAILED;
                logMessage = "Неизвестный статус платежа: " + status
                        + ". Подписка помечена как неактивная.";
                metaNode.put("action", "Деактивация по неизвестному/необрабатываемому статусу");
                metaNode.put("reason", "Неизвестный статус: " + status);
        }

        log.info("{} Пользователь: {}, Подписка: {}", logMessage,
                subscription.getUserId(), subscription.getType());

        subscription.setUpdatedAt(LocalDateTime.now());
        userSubscriptionRepository.save(subscription);
        createHistoryEntry(subscription.getUserId(), subscription.getType(), historyEventType, metaNode);
    }
}
