package ru.kata.project.infrastructure.webSocket;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Фоновая задача, которая регулярно выполняется (каждую минуту),
 * чтобы очищать неактивные WebSocket-сессии и освобождать ресурсы.
 */
@Component
public class WebSocketSessionCleanupTask {
    private final NotificationWebSocketHandler handler;

    public WebSocketSessionCleanupTask(NotificationWebSocketHandler handler) {
        this.handler = handler;
    }

    /**
     * Удаляет закрытые или устаревшие соединения
     * (те, что были открыты более 5 минут назад или были закрыты).
     */
    @Scheduled(fixedRate = 60_000) // каждые 60 секунд
    public void cleanUp() {
        handler.cleanupInactiveSessions(Duration.ofMinutes(5));
    }
}