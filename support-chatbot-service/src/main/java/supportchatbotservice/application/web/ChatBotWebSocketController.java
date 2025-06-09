package supportchatbotservice.application.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import supportchatbotservice.application.dto.ChatRequestDTO;
import supportchatbotservice.application.dto.ErrorResponseDTO;
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
public class ChatBotWebSocketController {
    private final ChatBotAppService chatBotAppService;

    @ExceptionHandler({AiServiceException.class, DomainValidationException.class})
    public ResponseEntity<ErrorResponseDTO> handleCustomExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @MessageMapping("/chat")
    public void handleChatMessage(ChatRequestDTO request, Principal principal) {
        chatBotAppService.handleUserMessage(request, principal);
    }
}