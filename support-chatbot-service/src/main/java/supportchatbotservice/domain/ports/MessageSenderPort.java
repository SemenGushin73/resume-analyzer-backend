package supportchatbotservice.domain.ports;

import supportchatbotservice.application.dto.ChatResponseDTO;

/**
 * Порт для отправки сообщений через WebSocket.
 */
public interface MessageSenderPort {
    void sendResponse(String user, ChatResponseDTO response);

    void sendError(String user, String error);
}