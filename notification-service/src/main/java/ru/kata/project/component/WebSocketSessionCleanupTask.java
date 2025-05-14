package ru.kata.project.component;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Класс выполняет следующую фоновую задачу (Scheduled Task):
 * Удаление "мертвых" сессий, которые неактивны дольше заданного времени.
 * Предотвращение утечек памяти и накопления неиспользуемых сессий.
 */
@Component
public class WebSocketSessionCleanupTask {
    private final WebSocketSessionRegistry registry;

    public WebSocketSessionCleanupTask(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Scheduled(fixedDelay = 120000)
    public void cleanOldSessions() {
        registry.cleanupOldSessions(Duration.ofMinutes(5));
    }
}
