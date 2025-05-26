package ru.kata.project.infrastructure.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import ru.kata.project.application.service.KafkaConsumerService;
import ru.kata.project.kafka.AnalysisResult;
import org.springframework.web.reactive.socket.WebSocketMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Обработчик WebSocket-соединений, реализующий интерфейс WebSocketHandler из Spring WebFlux.
 * Регистрирует клиентов, слушает сообщения с requestId, и отправляет соответствующий результат
 * анализа по WebSocket, когда он будет готов (из Kafka).
 */
@Slf4j
@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    private final Map<String, WebSocketSession> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, String> requestIdToSessionId = new ConcurrentHashMap<>();
    private final KafkaConsumerService kafkaConsumerService;
    private final ObjectMapper objectMapper;

    public NotificationWebSocketHandler(@Lazy KafkaConsumerService kafkaConsumerService, ObjectMapper objectMapper) {
        this.kafkaConsumerService = kafkaConsumerService;
        this.objectMapper = objectMapper.copy();
    }

    /**
     * Метод вызывается при подключении клиента по WebSocket.
     * Начинает слушать входящие сообщения (session.receive()).
     *
     * @param session - получает sessionId и сохраняет сессию.
     * @return Mono<Void> удаляет сессию из Map, удаляет все requestId, связанные с этой сессией.
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        final String sessionId = session.getId();
        sessionsById.put(sessionId, session);
        log.info("WebSocket connected: {}", sessionId);
        final Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(requestId -> {
                    log.info("Received requestId {} from session {}", requestId, sessionId);
                    requestIdToSessionId.put(requestId, sessionId);
                    // Попробуем сразу ответить, если результат уже есть
                    final AnalysisResult result = kafkaConsumerService.getResult(requestId);
                    if (result != null) {
                        sendResult(session, result);
                    }
                })
                .doOnError(e -> log.error("WebSocket receive error", e))
                .then();
        return input.doFinally(signal -> {
            sessionsById.remove(sessionId);
            requestIdToSessionId.values().removeIf(v -> v.equals(sessionId));
            log.info("WebSocket disconnected: {}", sessionId);
        });
    }

    /**
     * Метод вызывается Kafka-сервисом, когда получен результат анализа.
     *
     * @param requestId получает результат анализа по requestId.
     *                  Находит sessionId, ждущий этот результат.
     *                  Проверяет, открыта ли сессия.
     *                  Отправляет результат через sendResult(...), если всё валидно.
     */
    public void notifyClient(String requestId) {
        final AnalysisResult result = kafkaConsumerService.getResult(requestId);
        final String sessionId = requestIdToSessionId.get(requestId);
        if (result != null && sessionId != null) {
            final WebSocketSession session = sessionsById.get(sessionId);
            if (session != null && session.isOpen()) {
                sendResult(session, result);
            } else {
                log.warn("Session not found or closed for requestId {}", requestId);
            }
        } else {
            log.warn("No client registered for requestId {}", requestId);
        }
    }

    /**
     * Сериализует результат в JSON и отправляет сообщение клиенту по WebSocket.
     *
     * @param session отправляет JSON как текстовое сообщение
     * @param result объект AnalysisResult
     */
    private void sendResult(WebSocketSession session, AnalysisResult result) {
        try {
            final String json = objectMapper.writeValueAsString(result);
            session.send(Mono.just(session.textMessage(json)))
                    .doOnSuccess(v -> log.info("Sent result to session {}: {}", session.getId(), result.getRequestId()))
                    .doOnError(e -> log.error("Error sending result to session {}", session.getId(), e))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error serializing result for WebSocket", e);
        }
    }

    /**
     * Этот метод вызывается по расписанию (например, 1 раз в минуту)
     * и очищает закрытые или устаревшие WebSocket-сессии.
     * @param maxLifetime максимальное время подключения
     */
    public void cleanupInactiveSessions(Duration maxLifetime) {
        sessionsById.entrySet().removeIf(entry -> {
            final WebSocketSession session = entry.getValue();
            final boolean shouldRemove = !session.isOpen();
            if (shouldRemove) {
                requestIdToSessionId.values().removeIf(id -> id.equals(entry.getKey()));
                log.info("Cleaned up session: {}", entry.getKey());
            }
            return shouldRemove;
        });
    }
}