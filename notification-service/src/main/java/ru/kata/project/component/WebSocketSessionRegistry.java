package ru.kata.project.component;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import ru.kata.project.domain.RegisteredSession;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище активных WebSocket-сессий
 */
@Component
@Data
public class WebSocketSessionRegistry {
    private final Map<String, RegisteredSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> requestIdToSessionId = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

    public void registerSession(String requestId, WebSocketSession session) {
        sessions.put(session.getId(), new RegisteredSession(session, Instant.now()));
        requestIdToSessionId.put(requestId, session.getId());
    }

    public WebSocketSession getSessionByRequestId(String requestId) {
        String sessionId = requestIdToSessionId.get(requestId);
        RegisteredSession registeredSession = sessions.get(sessionId);
        return registeredSession != null ? registeredSession.getSession() : null;
    }

    public void removeSession(String requestId) {
        sessions.remove(requestId);
    }

    public void cleanupOldSessions(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        sessions.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getConnectedAt().isBefore(cutoff);
            if (expired) {
                String sessionId = entry.getKey();
                requestIdToSessionId.values().remove(sessionId);
            }
            return expired;
        });
    }
}
