package supportchatbotservice.application.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import supportchatbotservice.application.dto.ChatRequestDTO;
import supportchatbotservice.application.service.ChatBotAppService;

import java.security.Principal;

/**
 * Тестовый контроллер для чата(fallback).
 */
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat API (REST)", description = "Test API for chat operations")
public class ChatTestController {
    private final ChatBotAppService chatBotAppService;

    public ChatTestController(ChatBotAppService chatBotAppService) {
        this.chatBotAppService = chatBotAppService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send chat message (REST test)")
    public ResponseEntity<String> sendMessage(@RequestBody ChatRequestDTO request) {
        Principal principal = () -> "test-user"; // Mock principal
        chatBotAppService.handleUserMessage(request, principal);
        return ResponseEntity.ok("Message sent");
    }
}