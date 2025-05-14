package ru.kata.project.config.webSocketConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import ru.kata.project.component.WebSocketSessionRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocketRouterConfig — конфигурационный класс приложения,
 * который настраивает обработку WebSocket-соединений. Он не использует аннотации @EnableWebSocket или @ServerEndpoint,
 * а вместо этого настраивает WebSocket через Spring WebFlux или Spring WebSocket API с программной маршрутизацией.
 */
@Configuration
public class WebSocketRouterConfig {
    @Bean
    public WebSocketSessionRegistry webSocketSessionRegistry() {
        return new WebSocketSessionRegistry();
    }

    @Bean
    public HandlerMapping webSocketMapping(WebSocketHandler notificationWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/notifications", notificationWebSocketHandler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(10); // порядок обработки
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}