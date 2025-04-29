package ru.kata.project.application.service;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.ResumeScoreRequest;
import ru.kata.project.kafka.config.KafkaTopics;

/**
 * Сервис для отправки данных в Kafka.
 */
@Service
@Slf4j
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    /**
     * KafkaTemplate для отправки сообщений в Kafka.
     */
    private final KafkaTemplate<String, AnalysisRequest> kafkaTemplate;
    private final KafkaTemplate<String, ResumeScoreRequest> kafkaScoreTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, AnalysisRequest> kafkaTemplate,
                                KafkaTemplate<String, ResumeScoreRequest> kafkaScoreTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaScoreTemplate = kafkaScoreTemplate;
    }

    public void sendScoreRequest(String resumeId) {
        ResumeScoreRequest request = new ResumeScoreRequest(resumeId);
        kafkaScoreTemplate.send(KafkaTopics.AI_RESUME_SCORE_REQUESTS, request);
        log.info("Отправлен запрос на оценку резюме {}", resumeId);
    }

    public void sendAnalysisRequest(String requestId, AnalysisRequest request) {
        log.info("Sending request to Kafka. Topic: {}, Request ID: {}, Data: {}",
                KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
        try {
            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
            log.info("Request successfully sent to Kafka. Topic: {}, Request ID: {}, Data: {}",
                    KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
        } catch (Exception e) {
            log.error("Error sending request to Kafka. Topic: {}, Request ID: {}, Data: {}",
                    KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request, e);
        }
    }
}