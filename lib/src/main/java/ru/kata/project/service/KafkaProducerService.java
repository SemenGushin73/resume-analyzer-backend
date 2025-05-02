package ru.kata.project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.kata.project.kafka.config.KafkaTopics;

@Component
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String key, String data) {
        try {
            kafkaTemplate.send(KafkaTopics.DEEPSEEK_GENERATION_RESULT, key, data);
            log.info("Data sent to Kafka. Topic: {}, Key: {}, Data: {}",
                    KafkaTopics.DEEPSEEK_GENERATION_RESULT, key, data);
        } catch (Exception e) {
            log.error("Failed to send data to Kafka. Topic: {}, Key: {}, Data: {}",
                    KafkaTopics.DEEPSEEK_GENERATION_RESULT, key, data, e);
        }
    }
}