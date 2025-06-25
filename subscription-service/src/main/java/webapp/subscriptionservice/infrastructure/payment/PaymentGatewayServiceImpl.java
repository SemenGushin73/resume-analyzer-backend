package webapp.subscriptionservice.infrastructure.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import webapp.subscriptionservice.application.util.LogSanitizer;
import webapp.subscriptionservice.domain.service.PaymentGatewayService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс отвечает за взаимодействие с внешним платежным шлюзом (например, YooKassa).
 * Реализует интерфейс PaymentGatewayService, предоставляя методы для
 * создания платежных сессий и отмены платежей. Включает логику для обработки
 * HTTP-запросов, маскирования чувствительных данных в логах и использования
 * Resilience4j для обеспечения отказоустойчивости (Circuit Breaker, Retry, TimeLimiter).
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @SuppressFBWarnings("EI2")
    private final OkHttpClient httpClient;
    @SuppressFBWarnings("EI2")
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
     * Регулярные выражения для поиска чувствительных данных
     **/
    private static final Pattern SENSITIVE_DATA_PATTERN = Pattern.compile(
            "\"(token|auth_key|secret|password|api_key|access_token)\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * URL приложения, куда YooKassa будет отправлять коллбэки
     **/
    private static final String PAYMENT_GATEWAY_CIRCUIT_BREAKER = "paymentGatewayCircuitBreaker";

    /**
     * Метод для отправки HTTP-запроса и обработки ответа от платежного шлюза.
     * Выполняет HTTP-запрос с использованием OkHttpClient и обрабатывает полученный ответ.
     * В случае неуспешного ответа (не 2xx статус), логирует ошибку и выбрасывает PaymentGatewayException.
     * Если тело ответа пустое или null, возвращает пустой JsonNode.
     *
     * @param request Объект Request OkHttp для выполнения.
     * @return Представление JsonNode тела ответа.
     * @throws PaymentGatewayException Если произошла ошибка во время HTTP-запроса,
     *                                 получен неуспешный ответ от шлюза или возникла ошибка обработки ответа.
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private JsonNode sendHttpRequest(Request request) throws PaymentGatewayException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                final String responseBody;
                if (response.body() != null) {
                    responseBody = response.body().string();
                } else {
                    responseBody = "нет ответа";
                }
                log.error("Ошибка при выполнении запроса к платежному шлюзу. Status: {}, Body: {}",
                        LogSanitizer.sanitizeLogInput(String.valueOf(response.code())), LogSanitizer.sanitizeLogInput(maskSensitiveData(responseBody)));
                throw new PaymentGatewayException("Ошибка при взаимодействии с платежным шлюзом: " + responseBody);
            }

            final String responseBody;
            if (response.body() != null) {
                responseBody = response.body().string();
            } else {
                log.warn("Пустое тело ответа от платежного шлюза, хотя запрос был успешным.");
                responseBody = "";
            }

            if (responseBody.isEmpty()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(responseBody);
        } catch (IOException e) {
            log.error("IO ошибка при выполнении HTTP запроса", e);
            throw new PaymentGatewayException("Ошибка сети или сериализации/десериализации при взаимодействии с платежным шлюзом", e);
        }
    }

    /**
     * Создает новую платежную сессию в YooKassa для оформления подписки.
     * Метод использует Circuit Breaker, Retry и TimeLimiter из Resilience4j
     * для повышения отказоустойчивости. Генерирует ключ идемпотентности,
     * формирует авторизационный заголовок и тело запроса, затем отправляет
     * запрос на создание платежа в YooKassa.
     *
     * @param userId      Уникальный идентификатор пользователя.
     * @param type        Тип подписки.
     * @param amount      Сумма платежа.
     * @param description Описание платежа.
     * @return CompletableFuture, который завершится с объектом PaymentSessionResponse
     * при успешном создании сессии.
     * @throws PaymentGatewayException В случае ошибки взаимодействия с платежным шлюзом.
     * @throws Exception               В случае любой другой непредвиденной ошибки.
     */
    @CircuitBreaker(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "createPaymentSessionFallback")
    @Retry(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "createPaymentSessionFallback")
    @TimeLimiter(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "createPaymentSessionFallback")
    @Override
    public CompletableFuture<PaymentGatewayService.PaymentSessionResponse> createPaymentSession(
            UUID userId, SubscriptionType type, double amount, String description) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String idempotenceKey = UUID.randomUUID().toString();
                final String authHeader = "Basic " + Base64.getEncoder()
                        .encodeToString((yookassaShopId + ":" + yookassaSecretKey).getBytes(StandardCharsets.UTF_8));

                final long amountValue = (long) (amount * 100);
                final String requestBodyJson = buildPaymentRequestBodyJson(
                        amountValue, appCallbackUrl, userId.toString(), type.name(), description);

                final RequestBody body = RequestBody.create(requestBodyJson, MediaType.get("application/json; charset=utf-8"));
                final Request request = new Request.Builder()
                        .url(yookassaBaseUrl + "/payments")
                        .header("Idempotence-Key", idempotenceKey)
                        .header("Authorization", authHeader)
                        .post(body)
                        .build();

                final JsonNode rootNode = sendHttpRequest(request);

                final String paymentId = rootNode.path("id").asText();
                final String confirmationUrl = rootNode.path("confirmation").path("confirmation_url").asText();
                final String qrCode = rootNode.path("confirmation").path("qr_code").asText();

                return new PaymentGatewayService.PaymentSessionResponse(paymentId, confirmationUrl, qrCode);

            } catch (PaymentGatewayException e) {
                throw e;
            } catch (Exception e) {
                log.error("Исключение при создании платежной сессии", e);
                throw new PaymentGatewayException("Ошибка при создании платежной сессии", e);
            }
        });
    }

    /**
     * Fallback метод для основного метода createPaymentSession.
     * Этот метод вызывается Resilience4j, если основной метод createPaymentSession
     * завершается неудачей из-за сбоя Circuit Breaker, превышения лимита времени или после нескольких повторных попыток.
     * Он логирует причину вызова и выбрасывает PaymentGatewayException,
     * указывая на временную недоступность платежного шлюза.
     *
     * @param userId      Идентификатор пользователя.
     * @param type        Тип подписки.
     * @param amount      Сумма платежа.
     * @param description Описание платежа.
     * @param t           Исходное исключение, вызвавшее срабатывание fallback.
     * @return Завершенный CompletableFuture с исключением PaymentGatewayException.
     */
    @SuppressFBWarnings("UPM")
    private CompletableFuture<PaymentSessionResponse> createPaymentSessionFallback(
            UUID userId, SubscriptionType type, double amount, String description, Throwable t) {
        log.warn("Вызван fallback для createPaymentSession из-за: {}", LogSanitizer.sanitizeLogInput(t.getMessage()));
        throw new PaymentGatewayException("Платежный шлюз временно недоступен. Попробуйте позже. Статус подписки установлен на CANCELLED.", t);
    }

    /**
     * Отменяет платеж в YooKassa.
     * Этот метод также использует механизмы отказоустойчивости Resilience4j.
     * В "sandbox" реализации он просто логирует попытку отмены
     * и возвращает true. В реальной интеграции здесь должен быть
     * выполнен запрос к API YooKassa для операции возврата средств (refund).
     *
     * @param paymentId Идентификатор платежа, который необходимо отменить.
     * @return CompletableFuture, содержащий Boolean (true если отмена успешна).
     * @throws PaymentGatewayException В случае ошибки взаимодействия с платежным шлюзом.
     * @throws Exception               В случае любой другой непредвиденной ошибки.
     */
    @CircuitBreaker(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "cancelPaymentFallback")
    @Retry(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "cancelPaymentFallback")
    @TimeLimiter(name = PAYMENT_GATEWAY_CIRCUIT_BREAKER, fallbackMethod = "cancelPaymentFallback")
    @Override
    public CompletableFuture<Boolean> cancelPayment(String paymentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String authHeader = "Basic " + Base64.getEncoder()
                        .encodeToString((yookassaShopId + ":" + yookassaSecretKey).getBytes(StandardCharsets.UTF_8));
                log.info("Попытка отменить платеж с ID: {}. (Симуляция для sandbox режима платежного шлюза YooKassa)", LogSanitizer.sanitizeLogInput(paymentId));

                /** В Sandbox режиме просто возвращаем true. В реальной интеграции для отмены платежа нужно отправлять запрос
                 * на /refunds, а не просто возвращать true.
                 **/
                return true; // Временно возвращаем true для sandbox
            } catch (PaymentGatewayException e) {
                throw e;
            } catch (Exception e) {
                log.error("Исключение при отмене платежа Yookassa", e);
                throw new PaymentGatewayException("Ошибка отмены платежа", e);
            }
        });
    }

    /**
     * Fallback метод для основного метода cancelPayment.
     * Этот метод вызывается Resilience4j, если основной метод {@code cancelPayment}
     * завершается неудачей. Он логирует причину вызова и выбрасывает
     * PaymentGatewayException, указывая на временную недоступность платежного шлюза для отмены.
     *
     * @param paymentId Идентификатор платежа, который пытались отменить.
     * @param t         Исходное исключение, вызвавшее срабатывание fallback.
     * @return Завершенный CompletableFuture с исключением PaymentGatewayException.
     */
    @SuppressFBWarnings("UPM")
    private CompletableFuture<Boolean> cancelPaymentFallback(String paymentId, Throwable t) {
        log.warn("Вызван fallback для cancelPayment из-за: {}", LogSanitizer.sanitizeLogInput(t.getMessage()));
        throw new PaymentGatewayException("Платежный шлюз временно недоступен для отмены. Попробуйте позже.", t);
    }

    /**
     * Вспомогательный метод для построения JSON-тела запроса на создание платежа.
     * Формирует структуру JSON, необходимую для API YooKassa, включая сумму,
     * URL возврата для подтверждения, описание и метаданные платежа.
     *
     * @param amountValue Сумма платежа в копейках (например, 10000 для 100.00 RUB).
     * @param callbackUrl URL коллбэка приложения для перенаправления пользователя после оплаты.
     * @param userId      Идентификатор пользователя, связанный с подпиской.
     * @param type        Тип подписки (например, "BASIC", "PRO").
     * @param description Описание платежа, которое будет отображаться в платежном шлюзе.
     * @return JSON-строка для тела запроса создания платежа.
     */
    private String buildPaymentRequestBodyJson(long amountValue, String callbackUrl,
                                               String userId, String type, String description) {
        return String.format(
                "{" +
                        "\"amount\": {\"value\": \"%s.00\", \"currency\": \"RUB\"}," +
                        "\"capture\": true," +
                        "\"confirmation\": {\"type\": \"redirect\", \"return_url\": \"%s/subscriptions/success?userId=%s&type=%s\"}," +
                        "\"description\": \"%s\"," +
                        "\"metadata\": {\"userId\": \"%s\", \"subscriptionType\": \"%s\", \"service\": \"subscription-service\"}" +
                        "}",
                amountValue, callbackUrl, userId, type, description, userId, type
        );
    }

    /**
     * Маскирует чувствительные данные в строке JSON и заменяет их значения на маскированную строку "[***MASKED***]",
     * для предотвращения утечки конфиденциальной информации в логах.
     *
     * @param jsonBody JSON-строка, которую нужно маскировать.
     * @return JSON-строка с маскированными чувствительными данными.
     */
    private String maskSensitiveData(String jsonBody) {
        if (jsonBody == null || jsonBody.isEmpty()) {
            return jsonBody;
        }
        final Matcher matcher = SENSITIVE_DATA_PATTERN.matcher(jsonBody);
        final StringBuffer maskedBody = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(maskedBody, "\"" + matcher.group(1) + "\":\"[***MASKED***]\"");
        }
        matcher.appendTail(maskedBody);
        return maskedBody.toString();
    }

}
