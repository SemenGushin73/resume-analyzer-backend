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
     * Метод для создания новой подписки.
     * Проверяет нет ли активной подписки,
     * создает ее и присваивает начальный статус CANCELED, т.к. подписка еще не оплачена.
     * Создает платежную сессию и вызывает paymentGatewayService,
     * обновляет UserSubscription,
     * создает запись в SubscriptionHistory
     * Возвращает PaymentSessionResponseDto
     **/
    @Transactional
    @Override
    public PaymentSessionResponseDto createSubscription(UUID userId, SubscriptionType type) throws ExecutionException, InterruptedException {
        final Optional<UserSubscription> existingSubscription = userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        if (existingSubscription.isPresent()) {
            throw new IllegalArgumentException("У пользователя уже есть активная подписка");
        }

        final UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setType(type);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setPaymentProvider(PaymentProvider.YOOKASSA);

        final double amount = (type == SubscriptionType.BASIC) ? 100.00 : 500.00; // стоимость для примера
        final String description = String.format("%s Подписка для пользователя %s", type.name(), userId.toString());

        final PaymentGatewayService.PaymentSessionResponse paymentResponse;
        try {
            paymentResponse = paymentGatewayService.createPaymentSession(userId, type, amount, description).get();
        } catch (PaymentGatewayException e) {
            log.error("Ошибка при создании платежной сессии с шлюзом для пользователя userId {}: {}", userId, e.getMessage());

            ObjectNode metaNode = objectMapper.createObjectNode();
            metaNode.put("error", "Платежный шлюз недоступен");
            metaNode.put("details", e.getMessage());

            userSubscriptionRepository.save(subscription);
            subscriptionHistoryRepository.save(new SubscriptionHistory(
                    userId,
                    type,
                    SubscriptionEventType.CANCELLED,
                    metaNode
            ));
            throw e;
        }

        subscription.setExternalPaymentId(paymentResponse.paymentId());
        subscription.setPaymentSessionUrl(paymentResponse.confirmationUrl());
        subscription.setQrCodeData(paymentResponse.qrCodeData());

        final UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        ObjectNode metaNode = objectMapper.createObjectNode();
        metaNode.put("message", "Платежная сессия создана");
        metaNode.put("paymentId", paymentResponse.paymentId());

        subscriptionHistoryRepository.save(new SubscriptionHistory(
                userId,
                type,
                SubscriptionEventType.CREATED,
                metaNode
        ));

        log.info("Подписка создана для пользователя userId: {}, type: {}. Payment URL: {}. Ожидание коллбэка о платеже для активации.", userId, type, paymentResponse.confirmationUrl());
        return new PaymentSessionResponseDto(paymentResponse.paymentId(), paymentResponse.confirmationUrl(), paymentResponse.qrCodeData());
    }

    /**
     * Метод для отмены подписки:
     * Ищет активную подписку
     * Отмена платежа
     * Обновление UserSubscription
     * Запись в историю SubscriptionHistory
     **/
    @Transactional
    @Override
    public void cancelSubscription(UUID userId) throws ExecutionException, InterruptedException {
        final UserSubscription activeSubscription = userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Aктивная подписка не найдена: " + userId));

        try {
            final boolean cancelledInGateway = paymentGatewayService.cancelPayment(activeSubscription.getExternalPaymentId()).get();
            if (!cancelledInGateway) {
                log.warn("Ошибка отмены платежа в шлюзе для subscription ID: {}", activeSubscription.getId());
            }
        } catch (PaymentGatewayException e) {
            log.error("Ошибка отмены платежа в шлюзе для пользователя userId {}: {}", userId, e.getMessage());

            ObjectNode metaNode = objectMapper.createObjectNode();
            metaNode.put("error", "Ошибка отмены платежа");
            metaNode.put("details", e.getMessage());

            activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
            activeSubscription.setExpiresAt(LocalDateTime.now());
            activeSubscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(activeSubscription);

            subscriptionHistoryRepository.save(new SubscriptionHistory(
                    userId,
                    activeSubscription.getType(),
                    SubscriptionEventType.CANCELLED,
                    metaNode  // Используем JsonNode вместо String
            ));
            throw e;
        }

        activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
        activeSubscription.setExpiresAt(LocalDateTime.now());
        activeSubscription.setUpdatedAt(LocalDateTime.now());
        userSubscriptionRepository.save(activeSubscription);
        ObjectNode metaNode = objectMapper.createObjectNode();
        metaNode.put("message", "Подписка отменена пользователем");

        subscriptionHistoryRepository.save(new SubscriptionHistory(
                userId,
                activeSubscription.getType(),
                SubscriptionEventType.CANCELLED,
                metaNode  // Используем JsonNode вместо String
        ));
        log.info("Отменена подписка для пользователя userId: {}", userId);
    }

    /**
     * Метод для получения истории подписок пользователя:
     **/
    @Transactional(readOnly = true)
    @Override
    public List<SubscriptionHistoryDto> getSubscriptionHistory(UUID userId) {
        final List<SubscriptionHistory> history = subscriptionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return history.stream()
                .map(subscriptionMapper::toHistoryDto)
                .toList();
    }

    /**
     * Метод для получения информации об активной подписке пользователя
     **/
    @Transactional(readOnly = true)
    @Override
    public Optional<UserSubscriptionDto> getActiveSubscription(UUID userId) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(subscriptionMapper::toUserSubscriptionDto);
    }

    /**
     * Метод для обработки колбэков от платежного шлюза о статусе платежа.
     * Поиск подписки
     * Обработка статуса платежа
     * Обновление подписки
     * Запись в историю
     **/
    @Transactional
    @Override
    public void handlePaymentCallback(String paymentId, String status, String metadataJson) {
        log.info("Обработка коллбэка о платеже для paymentId: {}, status: {}", paymentId, status);

        final UserSubscription subscription = userSubscriptionRepository.findByExternalPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.error("Не найдена подписка для платежа payment ID: {}", paymentId);
                    return new ResourceNotFoundException("Не найдена подписка для платежа payment ID: " + paymentId);
                });

        ObjectNode metaNode = objectMapper.createObjectNode();
        metaNode.put("paymentId", paymentId);
        metaNode.put("status", status);

        if (metadataJson != null && !metadataJson.isEmpty()) {
            try {
                JsonNode externalMeta = objectMapper.readTree(metadataJson);
                metaNode.set("externalData", externalMeta);
            } catch (JsonProcessingException e) {
                metaNode.put("parseError", "Ошибка парсинга metadata");
                metaNode.put("rawData", metadataJson);
            }
        }

        SubscriptionEventType historyEventType;
        String logMessage;

        switch (status) {
            case "succeeded":
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setExpiresAt(LocalDateTime.now().plusMonths(1));
                historyEventType = SubscriptionEventType.CREATED;
                logMessage = "Оплата прошла успешно. Подписка активирована.";
                metaNode.put("action", "Активация подписки");
                break;

            case "failed":
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscription.setExpiresAt(LocalDateTime.now());
                historyEventType = SubscriptionEventType.CANCELLED;
                logMessage = "Ошибка или отмена платежа. Подписка отменена.";
                metaNode.put("action", "Автоматическая отмена");
                break;

            default:
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                historyEventType = SubscriptionEventType.CANCELLED;
                logMessage = "Неизвестный статус платежа: " + status + ". Подписка отменена.";
                metaNode.put("action", "Отмена по неизвестному статусу");
                metaNode.put("reason", "Неизвестный статус: " + status);
        }

        log.info("{} Пользователь: {}, Подписка: {}", logMessage, subscription.getUserId(), subscription.getType());

        subscription.setUpdatedAt(LocalDateTime.now());
        userSubscriptionRepository.save(subscription);

        subscriptionHistoryRepository.save(new SubscriptionHistory(
                subscription.getUserId(),
                subscription.getType(),
                historyEventType,
                metaNode
        ));
    }
}
