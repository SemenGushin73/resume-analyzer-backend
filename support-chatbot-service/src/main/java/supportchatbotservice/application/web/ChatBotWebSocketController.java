package supportchatbotservice.application.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import supportchatbotservice.application.dto.ChatRequest;
import supportchatbotservice.application.service.ChatBotAppService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatBotWebSocketController {
    private final ChatBotAppService chatBotAppService;

    @MessageMapping("/chat")
    public void handleChatMessage(ChatRequest request, Principal principal) {
        chatBotAppService.handleUserMessage(request, principal);
    }
}