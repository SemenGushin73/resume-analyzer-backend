package supportchatbotservice.infrastructure.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Сервис для работы с FAQ из конфигурации.
 */
@Service
@RequiredArgsConstructor
public class FAQService {
    private final FAQProperties faqProperties;

    public Optional<String> findAnswer(String question) {
        final String normalizedQuestion = question.trim().toLowerCase();

        return faqProperties.getAnswers().entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(normalizedQuestion))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}