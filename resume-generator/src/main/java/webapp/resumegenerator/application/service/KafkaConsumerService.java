package webapp.resumegenerator.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.kata.project.model.Department;
import webapp.resumegenerator.domain.model.Template;

/**
 * Получение данных из Kafka
 */
@Component
@Slf4j
public class KafkaConsumerService {

    private final KafkaProducerService kafkaProducerService;
    private static final String RESUME_GENERATION_REQUESTS = "resume-generation-requests";

    @Autowired
    public KafkaConsumerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = RESUME_GENERATION_REQUESTS)
    void listener(String jsonData, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Processing message. Topic: {}, Key: {}, Data: {}", RESUME_GENERATION_REQUESTS, key, jsonData);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, String> data = objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {
            });
            log.info("Received message. Topic: {}, Key: {}, Data: {}", RESUME_GENERATION_REQUESTS, key, data);

            Template template = new Template();
            template.setContent(jsonData);
            template.setDepartment(Department.getDepartment(data.get("department")));

            kafkaProducerService.sendToKafka(key, template);
            log.info("Message successfully sent to Kafka. Topic: {}, Key: {}, Data: {}", RESUME_GENERATION_REQUESTS,
                    key, template);
        } catch (Exception e) {
            log.error("Error processing message. Topic: {}, Key: {}, Data: {}", RESUME_GENERATION_REQUESTS,
                    key, jsonData, e);
        }
    }
}
