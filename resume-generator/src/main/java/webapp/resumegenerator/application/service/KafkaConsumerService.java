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
import webapp.resumegenerator.domain.model.Department;
import webapp.resumegenerator.domain.model.Template;

/**
 * Получение данных из Kafka
 */
@Component
@Slf4j
public class KafkaConsumerService {

    public final KafkaProducerService kafkaProducerService;

    @Autowired
    public KafkaConsumerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = "resume-generation-requests")
    void listener(String jsonData, @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        System.out.println(jsonData);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, String> data = objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {});
            log.info("Received message [{}]", data);
            Template template = new Template();
            template.setContent(jsonData);
            template.setDepartment(Department.getDepartment(data.get("department")));
            kafkaProducerService.sendToKafka(key, template);
        } catch (Exception e) {
            System.err.println("Error deserializing JSON: " + e.getMessage());
        }
    }
}
