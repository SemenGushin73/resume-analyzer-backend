package supportchatbotservice.infrastructure.faq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Конфигурация для работы с вопросами/ответами(преобразование в Map).
 */
@ConfigurationProperties(prefix = "faq")
public class FAQProperties {
    private Map<String, String> answers;

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
}