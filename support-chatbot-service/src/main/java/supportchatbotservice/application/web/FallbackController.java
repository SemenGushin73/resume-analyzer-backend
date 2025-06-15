package supportchatbotservice.application.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Fallback API", description = "API for fallback scenarios")
public class FallbackController {

    @GetMapping("/chatbot")
    @Operation(summary = "Get chatbot fallback message",
            description = "Returns a message when chatbot service is unavailable",
            responses = {
                    @ApiResponse(responseCode = "503", description = "Service unavailable")
            })
    public ResponseEntity<String> chatbotFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Chatbot service is currently unavailable");
    }
}