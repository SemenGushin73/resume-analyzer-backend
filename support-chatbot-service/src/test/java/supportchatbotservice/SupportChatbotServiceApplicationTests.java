package supportchatbotservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import supportchatbotservice.infrastructure.config.DotenvInitializer;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
public class SupportChatbotServiceApplicationTests {

    @Test
    void whenValidRequest_thenReturnsAiAnswer() {
        // TODO: реализовать интеграционный тест через MockMvc или TestRestTemplate
    }

    @Test
    void whenFaqTriggered_thenReturnsFallback() {
        // TODO: протестировать FAQService или контроллер с вопросом из fallback-слов
    }
}