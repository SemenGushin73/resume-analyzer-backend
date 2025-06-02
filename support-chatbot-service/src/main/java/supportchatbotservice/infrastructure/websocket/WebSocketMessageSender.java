package supportchatbotservice.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import supportchatbotservice.application.dto.ChatResponse;
import supportchatbotservice.application.dto.ErrorResponse;
import supportchatbotservice.domain.ports.MessageSenderPort;

@Service
@RequiredArgsConstructor
public class WebSocketMessageSender implements MessageSenderPort {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendResponse(String user, ChatResponse response) {
        messagingTemplate.convertAndSendToUser(
                user,
                "/queue/replies",
                response
        );
    }

    @Override
    public void sendError(String user, String error) {
        messagingTemplate.convertAndSendToUser(
                user,
                "/queue/errors",
                new ErrorResponse(error)
        );
    }
}