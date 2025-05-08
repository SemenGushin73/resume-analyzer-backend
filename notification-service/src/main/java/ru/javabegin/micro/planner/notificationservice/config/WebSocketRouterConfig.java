package ru.javabegin.micro.planner.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import ru.javabegin.micro.planner.notificationservice.component.NotificationWebSocketHandler;

import java.util.Map;

@Configuration
public class WebSocketRouterConfig {
    @Bean
    public HandlerMapping webSocketMapping(NotificationWebSocketHandler handler) {
        return new SimpleUrlHandlerMapping(Map.of("/ws/notifications", handler), 10);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}