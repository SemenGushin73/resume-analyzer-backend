package ru.kata.project.webSocket.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kata.project.webSocket.service.WebSocketNotificationService;

@Slf4j
@Component
class KafkaWebSocketBridge {

    @Autowired
    private WebSocketNotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.notifications}")
    public void listenForNotifications(ConsumerRecord<String, String> record) {
        String requestId = record.key();
        String message = record.value();

        log.debug("Received Kafka message for requestId: {}", requestId);
        notificationService.sendNotification(requestId, message);
    }
}