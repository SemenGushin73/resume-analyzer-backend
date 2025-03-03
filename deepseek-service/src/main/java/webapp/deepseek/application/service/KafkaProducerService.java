package webapp.deepseek.application.service;


import com.example.kafkadto.config.KafkaTopics;
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
            kafkaTemplate.send(KafkaTopics.DEEPSEEK_GENERATION_RESULT, key, data);
            log.info("Sending data to Kafka. Topic: {}, Key: {}, Data: {}",
                    KafkaTopics.DEEPSEEK_GENERATION_RESULT, key, data);
        } catch (Exception e) {
            log.error("Error sending data to Kafka. Topic: {}, Key: {}, Data: {}",
                    KafkaTopics.DEEPSEEK_GENERATION_RESULT, key, data, e);
        }
    }
}
