package supportchatbotservice.application.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.kata.project.chatbotDTO.ChatRequestDTO;
import ru.kata.project.chatbotDTO.ErrorResponseDTO;
import supportchatbotservice.application.service.ChatBotAppService;
import supportchatbotservice.domain.exception.AiServiceException;
import supportchatbotservice.domain.exception.DomainValidationException;

import java.security.Principal;

/**
 * WebSocket-контроллер для обработки чат-сообщений от клиентов.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat API", description = "API for chat operations via WebSocket")
public class ChatBotWebSocketController {
    private final ChatBotAppService chatBotAppService;

    @ExceptionHandler({AiServiceException.class, DomainValidationException.class})
    public ResponseEntity<ErrorResponseDTO> handleCustomExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @MessageMapping("/chat")
    @Operation(summary = "Handle chat message",
            description = "Processes user chat message and returns response via WebSocket")
    public void handleChatMessage(ChatRequestDTO request, Principal principal) {
        log.info("WebSocket сообщение получено: {}", request.getMessage());
        chatBotAppService.handleUserMessage(request, principal);
    }
}