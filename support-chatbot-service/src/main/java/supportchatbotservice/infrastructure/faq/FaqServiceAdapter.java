package supportchatbotservice.infrastructure.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import supportchatbotservice.domain.ports.FaqServicePort;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FaqServiceAdapter implements FaqServicePort {
    private final FAQService faqService;

    @Override
    public Optional<String> findAnswer(String question) {
        return faqService.findAnswer(question);
    }
}