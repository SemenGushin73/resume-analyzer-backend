package webapp.subscriptionservice.domain.service;

import ru.kata.project.model.PaymentStatus;
import ru.kata.project.model.SubscriptionType;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface SubscriptionService {
    PaymentSessionResponseDto createSubscription(UUID userId, SubscriptionType type) throws ExecutionException, InterruptedException;

    void cancelSubscription(UUID userId) throws ExecutionException, InterruptedException;

    List<SubscriptionHistoryDto> getSubscriptionHistory(UUID userId);

    Optional<UserSubscriptionDto> getActiveSubscription(UUID userId);

    void handlePaymentCallback(String paymentId, PaymentStatus status, String metadataJson);
}
