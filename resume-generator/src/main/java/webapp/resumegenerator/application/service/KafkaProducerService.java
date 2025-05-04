package webapp.resumegenerator.application.service;

import lombok.extern.slf4j.Slf4j;
import ru.kata.project.kafka.config.KafkaTopics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import webapp.resumegenerator.domain.model.Template;

/**
 * Отправка данных в Kafka
 */
@Component
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Template> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Template> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String key, Template data) {
        log.info("Sending data to Kafka. Topic: {}, Key: {}, Data: {}", KafkaTopics.DEEPSEEK_GENERATION_REQUESTS, key, data);
        try {
            kafkaTemplate.send(KafkaTopics.DEEPSEEK_GENERATION_REQUESTS, key, data);  // Отправка в Kafka
            log.info("Data successfully sent to Kafka. Topic: {}, Key: {}, Data: {}", KafkaTopics.DEEPSEEK_GENERATION_REQUESTS,
                    key, data); // Логирование отправленных данных
        } catch (Exception e) {
            log.error("Error sending data to Kafka. Topic: {}, Key: {}, Data: {}", KafkaTopics.DEEPSEEK_GENERATION_REQUESTS,
                    key, data, e);  // Логирование ошибки
        }
    }
}