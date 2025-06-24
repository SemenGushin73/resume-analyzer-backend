package supportchatbotservice.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.kata.project.chatbotDTO.ChatResponseDTO;
import ru.kata.project.chatbotDTO.ErrorResponseDTO;
import supportchatbotservice.domain.ports.MessageSenderPort;

/**
 * Сервис для отправки сообщений через WebSocket.
 */
@Service
@RequiredArgsConstructor
public class WebSocketMessageSender implements MessageSenderPort {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendResponse(String user, ChatResponseDTO response) {
        messagingTemplate.convertAndSend("/topic/replies", response);
    }

    @Override
    public void sendError(String user, String error) {
        messagingTemplate.convertAndSend("/topic/errors", new ErrorResponseDTO(error));
    }
}