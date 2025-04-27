package webapp.deepseekservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import webapp.deepseek.DeepseekServiceApplication;
import webapp.deepseek.domain.ChatMessage;
import webapp.deepseek.infrastructure.client.OpenRouterApiClient;
import webapp.deepseek.infrastructure.client.OpenRouterRequest;
import webapp.deepseek.infrastructure.client.OpenRouterResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тестовый класс для проверки работы api-key от модели deepseek.
 */
@SpringBootTest(classes = DeepseekServiceApplication.class)
public class DeepseekAPIKeyTest {

    @Autowired
    private OpenRouterApiClient openRouterApiClient;

    @Value("${openrouter.api-key}")
    private String API_KEY;

    @Value("${openrouter.model-name}")
    private String MODEL_NAME;

    private OpenRouterRequest testRequest;

    @BeforeEach
    void setUp() {
        // Подготовка тестового запроса
        testRequest = new OpenRouterRequest();
        testRequest.setModel(MODEL_NAME);
        testRequest.setMessages(Collections.singletonList(
            new ChatMessage("user", "Hello, how are you?"))
        );

    }

    @Test
    @DisplayName("Проверка наличия API ключа в конфигурации")
    void testHaveValidApiKeyConfiguration() {
        assertNotNull(API_KEY, "API ключ должен быть настроен в конфигурации");
        assertFalse(API_KEY.isBlank(), "API ключ не должен быть пустым");
    }

    @Test
    @DisplayName("Проверка работоспособности API ключа")
    public void testPerformanceApiKey() throws Exception {

        //при неверном API ключе здесь выйдет исключение
        final OpenRouterResponse response = openRouterApiClient.generateCompletion(
            "Bearer " + API_KEY,
            MODEL_NAME,
            testRequest
        );

        //проверка ответа
        assertNotNull(response);
    }
}
