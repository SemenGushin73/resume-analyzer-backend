package supportchatbotservice.application.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import supportchatbotservice.application.dto.ChatRequestDTO;
import supportchatbotservice.application.service.ChatBotAppService;

import java.security.Principal;

/**
 * Контроллер для обработки чат-сообщений от клиентов через REST.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API", description = "Chat operations via REST")
public class ChatController {
    private final ChatBotAppService chatBotAppService;

    @PostMapping("/send")
    @Operation(summary = "Отправить сообщение в чат и получить ответ")
    public ResponseEntity<String> sendMessage(@RequestBody ChatRequestDTO request, Principal principal) {
        chatBotAppService.handleUserMessage(request, principal != null ? principal : () -> "rest-user");
        return ResponseEntity.ok("Сообщение обработано");
    }
}