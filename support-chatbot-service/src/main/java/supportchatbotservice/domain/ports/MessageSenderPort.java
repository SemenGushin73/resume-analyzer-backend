package supportchatbotservice.domain.ports;

import supportchatbotservice.application.dto.ChatResponse;

public interface MessageSenderPort {
    void sendResponse(String user, ChatResponse response);

    void sendError(String user, String error);
}