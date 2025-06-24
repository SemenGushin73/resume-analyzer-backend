package supportchatbotservice.infrastructure.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import supportchatbotservice.domain.ports.FaqServicePort;

import java.util.Optional;

/**
 * Адаптер для FAQ сервиса.
 */
@Service
@RequiredArgsConstructor
public class FAQServiceAdapter implements FaqServicePort {
    private final FAQService faqService;

    @Override
    @Cacheable(value = "faq", key = "#question.toLowerCase()")
    public Optional<String> findAnswer(String question) {
        return faqService.findAnswer(question);
    }
}