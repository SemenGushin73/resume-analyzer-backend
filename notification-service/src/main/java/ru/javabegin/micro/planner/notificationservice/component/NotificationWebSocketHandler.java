package ru.javabegin.micro.planner.notificationservice.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Mono;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    private final WebSocketSessionRegistry registry;
    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    public NotificationWebSocketHandler(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(requestId -> {
                    log.info("Registering session {} for requestId {}", session.getId(), requestId);
                    registry.registerSession(requestId, session);
                    log.info("session {} for requestId {} fully registered", session.getId(), requestId);
                })
                .then();
    }
}
