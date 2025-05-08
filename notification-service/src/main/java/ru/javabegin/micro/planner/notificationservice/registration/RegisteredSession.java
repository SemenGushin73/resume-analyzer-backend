package ru.javabegin.micro.planner.notificationservice.registration;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.time.Instant;

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
