package ru.kata.project.domain;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.time.Instant;

/**
 * Класс выполняет следующие задачи:
 * Инкапсулирует данные о WebSocket-сессии (объект WebSocketSession).
 * Фиксирует время подключения (Instant connectedAt).
 * Предоставляет доступ к данным через геттеры (getSession(), getConnectedAt()). Применяется в реестре активных сессий
 * WebSocketSessionRegistry, где хранится маппинг requestId → RegisteredSession.
 */
public class RegisteredSession {
    private final WebSocketSession session;
    private final Instant connectedAt;

    public RegisteredSession(WebSocketSession session, Instant connectedAt) {
        this.session = session;
        this.connectedAt = connectedAt;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }
}
