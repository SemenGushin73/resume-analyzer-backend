package ru.javabegin.micro.planner.notificationservice.component;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

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
