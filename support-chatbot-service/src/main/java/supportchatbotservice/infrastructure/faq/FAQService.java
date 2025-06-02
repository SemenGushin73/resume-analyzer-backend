package supportchatbotservice.infrastructure.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FAQService {

    private final FAQProperties faqProperties;

    public Optional<String> findAnswer(String question) {
        return Optional.ofNullable(faqProperties.getAnswers().get(question));
    }
}