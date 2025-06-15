package supportchatbotservice.application.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import supportchatbotservice.domain.ports.AiServicePort;

/**
 * Тестовый контроллер для чата(deepseek).
 */
@RestController
@RequiredArgsConstructor
public class DeepSeekTestController {
    private final AiServicePort aiService;

    @GetMapping("/test-deepseek")
    public String testDeepSeek(@RequestParam String message) {
        try {
            return aiService.getAiResponse(message);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}