package ru.kata.project.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.config.KafkaTopics;

/**
 * Класс является продюсером (producer) сообщений в Kafka.
 * Отправляет запросы на анализ резюме в топик RESUME_ANALYZE_REQUESTS.
 * Связывает каждый запрос с уникальным requestId для последующей обработки и отслеживания.
 */
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, AnalysisRequest> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, AnalysisRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAnalysisRequest(String requestId, AnalysisRequest request) {
        log.info("Sending request to Kafka. Topic: {}, Request ID: {}, Data: {}",
                KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
        try {
            String json = objectMapper.writeValueAsString(request);
            log.info("Sending request to Kafka. Topic: {}, Request ID: {}, Data: {}", KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, json);
            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
            log.info("Request successfully sent to Kafka. Topic: {}, Request ID: {}, Data: {}",
                    KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
        } catch (Exception e) {
            log.error("Error sending request to Kafka. Topic: {}, Request ID: {}, Data: {}",
                    KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request, e);
        }
    }
}
