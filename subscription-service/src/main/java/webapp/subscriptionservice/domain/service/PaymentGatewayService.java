package webapp.subscriptionservice.domain.service;

import ru.kata.project.model.SubscriptionType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс, определяющий контракт для взаимодействия с платежным шлюзом.
 * Служит "портом" из слоя бизнес-логики (Use Cases) к внешнему сервису.
 **/
public interface PaymentGatewayService {


    CompletableFuture<PaymentSessionResponse> createPaymentSession(UUID userId, SubscriptionType type, double amount, String description);

    CompletableFuture<Boolean> cancelPayment(String paymentId);

    record PaymentSessionResponse(String paymentId, String confirmationUrl, String qrCodeData) {
    }
}
