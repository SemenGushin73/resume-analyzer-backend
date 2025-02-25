package webapp.resumegenerator.application.service;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate <String, Template> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Template> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String key, Template data) {
        try {
            kafkaTemplate.send("deepseek-generation-requests", key, data);  // Отправка в Kafka
            logger.info("Data successfully sent to Kafka: {}", data); // Логирование отправленных данных
        } catch (Exception e) {
            logger.error("Error sending data to Kafka", e);  // Логирование ошибки
        }
    }
}
