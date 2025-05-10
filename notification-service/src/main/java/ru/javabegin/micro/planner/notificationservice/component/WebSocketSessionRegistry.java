package ru.javabegin.micro.planner.notificationservice.component;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import ru.javabegin.micro.planner.notificationservice.registration.RegisteredSession;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище активных WebSocket-сессий
 */

@Component
public class WebSocketSessionRegistry {
    private final Map<String, RegisteredSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> requestIdToSessionId = new ConcurrentHashMap<>();

    public void registerSession(String requestId, WebSocketSession session) {
        sessions.put(session.getId(), new RegisteredSession(session, Instant.now()));
        requestIdToSessionId.put(requestId, session.getId());
    }

    public WebSocketSession getSessionByRequestId(String requestId) {
        final String sessionId = requestIdToSessionId.get(requestId);
        if (sessionId != null) {
            final RegisteredSession reg = sessions.get(sessionId);
            if (reg != null) {
                return reg.getSession();
            } else return null;
        } else {
            return null;
        }
    }
    public void cleanupOldSessions(Duration maxAge) {
        final Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
                RegisteredSession reg = entry.getValue();
                boolean expired = Duration.between(reg.getConnectedAt(), now).compareTo(timeout) > 0;
            if (expired) {
                requestIdToSessionId.values().removeIf(id -> id.equals(entry.getKey()));
                try {
                    reg.getSession().close();
                } catch (IOException e) {
                    log.warn("Failed to close WebSocket session", e);
                }
            }
            return expired;
        });
    }
}
