package ru.kata.project.webSocket.component;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.time.Instant;

@Data
public class RegisteredSession {
    private final WebSocketSession session;
    private final Instant connectedAt;

    public RegisteredSession(WebSocketSession session) {
        this.session = session;
        this.connectedAt = Instant.now();
    }

    public WebSocketSession getSession() {
        return session;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }
}
