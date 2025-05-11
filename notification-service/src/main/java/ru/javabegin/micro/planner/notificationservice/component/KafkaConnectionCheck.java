package ru.javabegin.micro.planner.notificationservice.component;

import jakarta.annotation.PostConstruct;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.kata.project.kafka.config.KafkaTopics;

/**
 * Для проверки на каком порту Kafka. Если выводится localhost:9092 — точно идет переопределение.
 */
@Component
public class KafkaConnectionCheck {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaConnectionCheck(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void checkConnection() {
        try {
            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_REQUESTS, "ping");
            System.out.println("Kafka доступна");
        } catch (Exception e) {
            System.err.println("Ошибка подключения к Kafka: " + e.getMessage());
        }
    }
}