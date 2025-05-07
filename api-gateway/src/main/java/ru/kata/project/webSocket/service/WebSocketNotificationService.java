package ru.kata.project.webSocket.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.*;

@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);

    // Хранилище активных сессий: requestId -> WebSocketSession
    private final ConcurrentMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    // Обратное отображение для очистки: WebSocketSession -> requestId
    private final ConcurrentMap<WebSocketSession, String> sessionToRequestId = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public WebSocketNotificationService() {
        // Периодическая очистка неактивных сессий
        cleanupExecutor.scheduleAtFixedRate(this::cleanupInactiveSessions, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Регистрация новой WebSocket сессии
     */
    public void registerSession(String requestId, WebSocketSession session) {
        activeSessions.put(requestId, session);
        sessionToRequestId.put(session, requestId);
        log.debug("Registered session for requestId: {}", requestId);
    }

    /**
     * Удаление сессии
     */
    public void unregisterSession(WebSocketSession session) {
        String requestId = sessionToRequestId.remove(session);
        if (requestId != null) {
            activeSessions.remove(requestId);
            log.debug("Unregistered session for requestId: {}", requestId);
        }
    }

    /**
     * Отправка уведомления клиенту
     */
    public void sendNotification(String requestId, String message) {
        WebSocketSession session = activeSessions.get(requestId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                    log.debug("Sent notification for requestId: {}", requestId);
                }
            } catch (IOException e) {
                log.error("Failed to send WebSocket message", e);
                unregisterSession(session);
            }
        } else {
            log.warn("No active session found for requestId: {}", requestId);
            if (session != null) {
                unregisterSession(session);
            }
        }
    }

    /**
     * Очистка неактивных сессий
     */
    private void cleanupInactiveSessions() {
        int before = activeSessions.size();

        sessionToRequestId.keySet().removeIf(session -> !session.isOpen());
        activeSessions.entrySet().removeIf(entry -> !entry.getValue().isOpen());

        int after = activeSessions.size();
        if (before != after) {
            log.info("Cleaned up inactive sessions: {} -> {}", before, after);
        }
    }

    @PreDestroy
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}