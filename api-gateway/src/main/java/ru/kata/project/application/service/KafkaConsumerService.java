package ru.kata.project.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import ru.kata.project.kafka.AnalysisResult;
import ru.kata.project.kafka.ResumeScoreResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kata.project.kafka.config.KafkaTopics;

import java.util.HashMap;
import java.util.Map;

/**
 * Обрабатывает сообщения из Kafka, полученные от модулей системы.
 * Отслеживает результаты анализа резюме от DeepSeek-Service и передает их в клиентские запросы.
 */
@Service
@Slf4j
public class KafkaConsumerService {
    private final Map<String, AnalysisResult> results = new HashMap<>();
    private final Map<String, ResumeScoreResult> scoreResults = new HashMap<>();


    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_RESULTS)
    public void consume(AnalysisResult result) {
        log.info("Processing message from topic: {}. Request ID: {}, Data: {}",
                KafkaTopics.RESUME_ANALYZE_RESULTS, result.getRequestId(), result);

        try {
            results.put(result.getRequestId(), result);
        } catch (Exception e) {
            log.error("Error processing message from topic: {}. Request ID: {}, Data: {}",
                    KafkaTopics.RESUME_ANALYZE_RESULTS, result.getRequestId(), result, e);
        }
    }

    @KafkaListener(topics = KafkaTopics.AI_RESUME_SCORE_RESULTS, groupId = "gateway-result-group")
    public void consume(ResumeScoreResult result) {
        scoreResults.put(result.getResumeId(), result);
        log.info("Получен результат анализа резюме {}", result.getResumeId());
    }

    public ResumeScoreResult getScoreResult(String resumeId) {
        return scoreResults.get(resumeId);
    }

    public AnalysisResult getResult(String requestId) {
        return results.get(requestId);
    }

}