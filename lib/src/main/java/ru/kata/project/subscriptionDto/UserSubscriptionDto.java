package ru.kata.project.subscriptionDto;

import lombok.Data;
import ru.kata.project.model.PaymentProvider;
import ru.kata.project.model.SubscriptionStatus;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для передачи информации о текущей или прошлых подписках пользователя
 **/
@Data
public class UserSubscriptionDto {
    private UUID id;
    private UUID userId;
    private SubscriptionType type;
    private SubscriptionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private PaymentProvider paymentProvider;
    private String externalPaymentId;
    private String paymentSessionUrl;
    private String qrCodeData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}