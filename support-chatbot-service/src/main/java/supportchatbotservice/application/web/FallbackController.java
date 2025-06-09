package supportchatbotservice.application.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Контроллер для обработки запросов при недоступности основного сервиса.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/chatbot")
    public ResponseEntity<String> chatbotFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Chatbot service is currently unavailable");
    }
}