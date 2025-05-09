package webapp.resumeanalyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import webapp.resumeanalyzer.ResumeAnalyzerApplication;
import webapp.resumeanalyzer.application.utils.ai.AIMessageSender;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тестовый класс для проверки работы AI-сервиса.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@EmbeddedKafka
public class DeepseekAPIKeyTest {

    @Autowired
    private AIMessageSender aiMessageSender;

    private String testMessage;

    @BeforeEach
    void setUp() {
        testMessage = "Hello, how are you?";
    }

    @Test
    @DisplayName("Проверка работоспособности API ключа")
    public void testPerformanceApiKey() {
        // При неверном API ключе здесь выйдет исключение
        final String response = aiMessageSender.send(testMessage);
        
        // Проверка ответа
        assertNotNull(response);
        assertFalse(response.isBlank(), "Ответ не должен быть пустым");
    }
}