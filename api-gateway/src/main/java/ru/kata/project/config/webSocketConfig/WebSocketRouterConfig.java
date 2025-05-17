package ru.kata.project.config.webSocketConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import ru.kata.project.infrastructure.webSocket.NotificationWebSocketHandler;

import java.util.Map;

/**
 * Конфигурационный класс, регистрирует WebSocket-обработчик на определённый маршрут.
 * Он необходим для того, чтобы сервер знал, какой обработчик (handler) вызывать,
 * когда приходит WebSocket-соединение по определённому URL.
 */

@Configuration
public class WebSocketRouterConfig {
    private final NotificationWebSocketHandler handler;

    public WebSocketRouterConfig(NotificationWebSocketHandler handler) {
        this.handler = handler;
    }

    /**
     * Используется SimpleUrlHandlerMapping —
     * способ задать соответствие между URL и WebSocketHandler.
     *
     * @return Возвращает бин HandlerMapping, который сообщает Spring,
     * * что все запросы по пути /ws/notifications должны обрабатываться
     * * классом NotificationWebSocketHandler.
     */
    @Bean
    public HandlerMapping webSocketMapping() {
        return new SimpleUrlHandlerMapping(Map.of(
                "/ws/notifications", handler
        ), -1);
    }

    /**
     * WebSocketHandlerAdapter — это необходимый компонент,
     * адаптирующий ваш WebSocketHandler к инфраструктуре Spring WebFlux.
     *
     * @return регистрирует WebSocketHandlerAdapter
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
