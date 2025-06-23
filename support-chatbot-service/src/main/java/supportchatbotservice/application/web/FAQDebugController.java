package supportchatbotservice.application.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import supportchatbotservice.infrastructure.faq.FAQProperties;

import java.util.Map;

/**
 * Контроллер для проверки работоспособности FAQ через REST.
 */
@RestController
@RequestMapping("/api/internal/faq")
@Profile("dev")
@RequiredArgsConstructor
public class FAQDebugController {

    private final Map<String, String> faqAnswers;

    @Autowired
    public FAQDebugController(FAQProperties faqProperties) {
        this.faqAnswers = faqProperties.getAnswers();
    }

    @GetMapping
    public Map<String, String> getAllFaq() {
        return faqAnswers;
    }

    @GetMapping("/{questionKey}")
    public ResponseEntity<String> getFaqAnswer(@PathVariable String questionKey) {
        String answer = faqAnswers.get(questionKey);
        if (answer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(answer);
    }
}