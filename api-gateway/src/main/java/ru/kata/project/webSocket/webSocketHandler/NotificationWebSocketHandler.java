package ru.kata.project.webSocket.webSocketHandler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;


import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.kata.project.webSocket.service.WebSocketNotificationService;

import java.util.Arrays;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    @Autowired
    private WebSocketNotificationService notificationService;

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String requestId = extractRequestId(session);
        if (requestId != null) {
            notificationService.registerSession(requestId, (org.springframework.web.socket.WebSocketSession) session);
            log.info("WebSocket connected for requestId: {}", requestId);
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Missing requestId parameter"));
        }
    }

    private String extractRequestId(WebSocketSession session) {
        String query = String.valueOf(session.getHandshakeInfo().getUri().getQuery());
        if (query == null) return null;
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .filter(pair -> pair.length == 2 && "requestId".equals(pair[0]))
                .findFirst()
                .map(pair -> pair[1])
                .orElse(null);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        notificationService.unregisterSession((org.springframework.web.socket.WebSocketSession) session);
        log.info("WebSocket disconnected: {}", status);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("Received WebSocket message: {}", message.getPayload());
    }
}

