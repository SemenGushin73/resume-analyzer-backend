package ru.kata.project.component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.kata.project.kafka.config.KafkaTopics;

/**
 * Для проверки на каком порту Kafka. Если выводится localhost:9092 — точно идет переопределение.
 */
@Component
public class KafkaConnectionCheck {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(KafkaConnectionCheck.class);

    public KafkaConnectionCheck(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void checkConnection() {
        try {
            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_REQUESTS, "ping");
            log.info("Kafka доступна");
        } catch (Exception e) {
            log.error("Ошибка подключения к Kafka" + e.getMessage());
        }
    }
}