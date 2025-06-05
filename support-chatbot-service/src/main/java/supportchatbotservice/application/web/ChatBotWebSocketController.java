package supportchatbotservice.application.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import supportchatbotservice.application.dto.ChatRequest;
import supportchatbotservice.application.dto.ErrorResponse;
import supportchatbotservice.application.service.ChatBotAppService;
import supportchatbotservice.domain.exception.AiServiceException;
import supportchatbotservice.domain.exception.DomainValidationException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatBotWebSocketController {
    private final ChatBotAppService chatBotAppService;

    @ExceptionHandler({AiServiceException.class, DomainValidationException.class})
    public ResponseEntity<ErrorResponse> handleCustomExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @MessageMapping("/chat")
    public void handleChatMessage(ChatRequest request, Principal principal) {
        chatBotAppService.handleUserMessage(request, principal);
    }
}