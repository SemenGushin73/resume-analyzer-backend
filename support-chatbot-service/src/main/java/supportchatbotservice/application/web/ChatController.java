package supportchatbotservice.application.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kata.project.chatbotDTO.ChatRequestDTO;
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
    public ResponseEntity<String> sendMessage(@Valid @RequestBody ChatRequestDTO request, Principal principal) {
        final Principal safePrincipal;
        if (principal != null) {
            safePrincipal = principal;
        } else {
            safePrincipal = () -> "rest-user";
        }
        chatBotAppService.handleUserMessage(request, safePrincipal);
        return ResponseEntity.ok("Сообщение обработано");
    }
}