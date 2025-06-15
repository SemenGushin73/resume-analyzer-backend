package supportchatbotservice.infrastructure.faq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Тестовый контроллер для чата(faq).
 */
@RestController
@RequestMapping("/api/faq")
public class FAQTestController {

    private final Map<String, String> faqAnswers;

    @Autowired
    public FAQTestController(FAQProperties faqProperties) {
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