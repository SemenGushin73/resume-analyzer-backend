package ru.kata.project.webSocket.webSocketConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import ru.kata.project.webSocket.webSocketHandler.NotificationWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketRouterConfig {
@Bean
    public HandlerMapping handlerMapping(NotificationWebSocketHandler notificationWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/notification", notificationWebSocketHandler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(map);
        handlerMapping.setOrder(10);
        return handlerMapping;
    }
@Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
