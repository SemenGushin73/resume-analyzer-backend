package ru.javabegin.micro.planner.notificationservice.component;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import ru.javabegin.micro.planner.notificationservice.registration.RegisteredSession;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {
    private final Map<String, RegisteredSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> requestIdToSessionId = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session, String requestId) {
        sessions.put(session.getId(), new RegisteredSession(session, Instant.now()));
        requestIdToSessionId.put(requestId, session.getId());
    }

    public WebSocketSession getSessionByRequestId(String requestId) {
        String sessionId = requestIdToSessionId.get(requestId);
        RegisteredSession reg = sessions.get(sessionId);
        return reg != null ? reg.getSession() : null;
    }

    public void cleanupOldSessions(Duration maxAge) {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry ->
                Duration.between(entry.getValue().getConnectedAt(), now).compareTo(maxAge) > 0);
    }
}
