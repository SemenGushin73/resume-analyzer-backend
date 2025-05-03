package ru.kata.project.webSocket.component;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Создаем сервис, в котором будем хранить отображение
 */

public class WebSocketSessionRegistry {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> requestIdToSessionsId = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void bindRequestToSession(String requestId, String sessionId) {
        requestIdToSessionsId.put(requestId, sessionId);

    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        requestIdToSessionsId.values().removeIf(id -> id.equals(sessionId));
    }

    public Optional<WebSocketSession> getSessionByRequestId(String requestId) {
        return Optional.ofNullable(sessions.get(requestId))
                .map(sessions::get);
    }
}
