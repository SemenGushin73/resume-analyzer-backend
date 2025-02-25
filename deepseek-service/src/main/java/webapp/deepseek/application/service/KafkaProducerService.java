package webapp.deepseek.application.service;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Отправка данных в Kafka
 */
@Component
@Slf4j
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate <String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String key, String data) {
        try {
            kafkaTemplate.send("deepseek-generation-result", key, data);  // Отправка в Kafka
            logger.info("Data successfully sent to Kafka: {}", data); // Логирование отправленных данных
        } catch (Exception e) {
            logger.error("Error sending data to Kafka", e);  // Логирование ошибки
        }
    }
}
