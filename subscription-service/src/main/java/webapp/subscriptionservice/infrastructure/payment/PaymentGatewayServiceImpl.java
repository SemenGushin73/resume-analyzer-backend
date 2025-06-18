package webapp.subscriptionservice.infrastructure.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.kata.project.exception.PaymentGatewayException;
import ru.kata.project.model.SubscriptionType;
import webapp.subscriptionservice.domain.service.PaymentGatewayService;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Класс отвечает за взаимодействие с внешним платежным шлюзом
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Данные для YooKassa берутся из application.yml
     **/
    @Value("${payment.yookassa.shop-id}")
    private String yookassaShopId;

    /**
     * ID магазина
     **/
    @Value("${payment.yookassa.secret-key}")
    private String yookassaSecretKey;

    /**
     * Секретный ключ для авторизации в YooKassa
     **/
    @Value("${payment.yookassa.base-url}")
    private String yookassaBaseUrl;

    /**
     * Базовый URL API YooKassa. Одинаковый как для реального, так и для sandbox
     **/
    @Value("${app.callback-url}")
    private String appCallbackUrl;

    /**
     * URL приложения, куда YooKassa будет отправлять коллбэки
     **/
    private static final String PAYMENT_GATEWAY_CIRCUIT_BREAKER = "paymentGatewayCircuitBreaker";

    /**
     * Метод для создания новой платежной сессии в YooKassa для оформления подписки
     **/
    @CircuitBreaker(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "createPaymentSessionFallback")
    @Retry(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "createPaymentSessionFallback")
    @TimeLimiter(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "createPaymentSessionFallback")
    @Override
    public CompletableFuture<PaymentGatewayService.PaymentSessionResponse> createPaymentSession(UUID userId, SubscriptionType type, double amount, String description) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String idempotenceKey = UUID.randomUUID().toString();
                final String authHeader = "Basic " + Base64.getEncoder().encodeToString((yookassaShopId + ":" + yookassaSecretKey).getBytes());

                final long amountValue = (long) (amount * 100);

                final String requestBodyJson = String.format(
                        "{" +
                                "\"amount\": {\"value\": \"%s.00\", \"currency\": \"RUB\"}," +
                                "\"capture\": true," +
                                "\"confirmation\": {\"type\": \"redirect\", \"return_url\": \"%s/subscriptions/success?userId=%s&type=%s\"}," +
                                "\"description\": \"%s\"," +
                                "\"metadata\": {\"userId\": \"%s\", \"subscriptionType\": \"%s\", \"service\": \"subscription-service\"}" +
                                "}",
                        amountValue, appCallbackUrl, userId.toString(), type.name(), description, userId.toString(), type.name()
                );

                final RequestBody body = RequestBody.create(requestBodyJson, MediaType.get("application/json; charset=utf-8"));
                final Request request = new Request.Builder()
                        .url(yookassaBaseUrl + "/payments")
                        .header("Idempotence-Key", idempotenceKey)
                        .header("Authorization", authHeader)
                        .post(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        final String responseBody = response.body() != null ? response.body().string() : "нет ответа";
                        log.error("Ошибка при создании платежной сессии Yookassa. Status: {}, Body: {}", response.code(), responseBody);
                        throw new PaymentGatewayException("Ошибка при создании платежной сессии: " + responseBody);
                    }

                    final String responseBody = response.body().string();
                    final JsonNode rootNode = objectMapper.readTree(responseBody);

                    final String paymentId = rootNode.path("id").asText();
                    final String confirmationUrl = rootNode.path("confirmation").path("confirmation_url").asText();
                    final String qrCode = rootNode.path("confirmation").path("qr_code").asText();

                    return new PaymentGatewayService.PaymentSessionResponse(paymentId, confirmationUrl, qrCode);

                } catch (IOException e) {
                    log.error("IO ошибка при создании платежной сессии", e);
                    throw new PaymentGatewayException("IO при создании платежной сессии", e);
                }
            } catch (Exception e) {
                log.error("Exception при создании платежной сессии", e);
                throw new PaymentGatewayException("Ошибка при создании платежной сессии", e);
            }
        });
    }

    /**
     * Fallback метод для основного метода создания платежной сессии.
     * Вызывается, если основной метод createPaymentSession завершается неудачей.
     * Логирует причину вызова и выбрасывает исключение
     **/
    private CompletableFuture<PaymentSessionResponse> createPaymentSessionFallback(UUID userId, SubscriptionType type, double amount, String description, Throwable t) {
        log.warn("Fallback triggered for createPaymentSession due to: {}", t.getMessage());
        throw new PaymentGatewayException("Платежный шлюз временно недоступен. Попробуйте позже. Статус подписки установлен на CANCELLED.", t);
    }

    /**
     * Метод для отмены платежа в YooKassa
     **/
    @CircuitBreaker(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "cancelPaymentFallback")
    @Retry(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "cancelPaymentFallback")
    @TimeLimiter(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "cancelPaymentFallback")
    @Override
    public CompletableFuture<Boolean> cancelPayment(String paymentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String authHeader = "Basic " + Base64.getEncoder().encodeToString((yookassaShopId + ":" + yookassaSecretKey).getBytes());
                log.info("Попытка отменить платеж с ID: {}. (Симуляция для sandbox режима платежного шлюза YooKassa)", paymentId);

                /** В Sandbox режиме просто возвращаем true. В реальной интеграции для отмены платежа нужно отправлять запрос
                 на /refunds, а не просто возвращать true. **/
                return true; // Временно возвращаем true для sandbox
            } catch (Exception e) {
                log.error("Exception при отмене платежа Yookassa", e);
                throw new PaymentGatewayException("Ошибка отмены платежа", e);
            }
        });
    }

    /**
     * Fallback метод для основного метода отмены платежа.
     * Вызывается, если основной метод завершается неудачей.
     * Логирует причину вызова и выбрасывает исключение
     **/
    private CompletableFuture<Boolean> cancelPaymentFallback(String paymentId, Throwable t) {
        log.warn("Fallback triggered for cancelPayment due to: {}", t.getMessage());
        throw new PaymentGatewayException("Платежный шлюз временно недоступен для отмены. Попробуйте позже.", t);
    }

}
