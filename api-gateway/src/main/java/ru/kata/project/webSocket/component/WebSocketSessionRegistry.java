package ru.kata.project.webSocket.component;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Создаем сервис для хранения и управления активными WebSocket-сессиями
 */
@Component
public class WebSocketSessionRegistry {
    private final Map<String, RegisteredSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> requestIdToSessionsId = new ConcurrentHashMap<>();
    private final Duration maxIdleTime = Duration.ofMinutes(30);

    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, new RegisteredSession(session));
    }

    public void bindRequestToSession(String requestId, String sessionId) {
        requestIdToSessionsId.put(requestId, sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        requestIdToSessionsId.values().removeIf(id -> id.equals(sessionId));
    }

    public Optional<WebSocketSession> getSessionByRequestId(String requestId) {
        final String sessionId = requestIdToSessionsId.get(requestId);
        if (sessionId == null) {
            return Optional.empty();
        }
        final RegisteredSession registeredSession = sessions.get(sessionId);
        return registeredSession != null ? Optional.of(registeredSession.getSession()) : Optional.empty();
    }

    public void clearIdleSessions() {
        final Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
            final Instant connectedAt = entry.getValue().getConnectedAt();
            final Duration timeElapsed = Duration.between(connectedAt, now);
            return timeElapsed.compareTo(maxIdleTime) > 0;
        });
        // Чистим связанные requestId
        requestIdToSessionsId.entrySet().removeIf(entry -> !sessions.containsKey(entry.getValue()));
    }
}
