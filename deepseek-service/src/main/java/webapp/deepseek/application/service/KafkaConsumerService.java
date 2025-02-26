package webapp.deepseek.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import webapp.deepseek.infrastructure.client.model.Department;

/**
 * Получение данных из Kafka
 */
@Component
@Slf4j
public class KafkaConsumerService {

    private final DeepSeekServiceImpl deepSeekService;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public KafkaConsumerService(DeepSeekServiceImpl deepSeekService,
                                KafkaProducerService kafkaProducerService) {
        this.deepSeekService = deepSeekService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = "deepseek-generation-requests")
    void listener(String jsonData, @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> data = objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {});
            log.info("Received message [{}]", data);
            String response = deepSeekService.generateResume(data.get("content"),
                    Department.getDepartment(data.get("department")).getDescription());
            kafkaProducerService.sendToKafka(key, response);
        } catch (Exception e) {
            System.err.println("Error deserializing JSON: " + e.getMessage());
        }
    }
}
