package webapp.resumeanalyzer.application.service;

import com.example.kafkadto.config.KafkaTopics;
import com.example.kafkadto.dto.AnalysisRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    private final KafkaTemplate<String, AnalysisRequest> kafkaTemplate;

    @Autowired
    public KafkaConsumerService(KafkaTemplate<String, AnalysisRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_REQUESTS)
    public void AnalysisRequest(@Header("kafka_receivedMessageKey") String requestId, AnalysisRequest request) {
        log.info("Получен запрос для анализа. Topic: {}, requestId: {}, данные: {}",
                KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);

        if (isValid(request.getResumeText()) && isValid(request.getJobDescription())) {
            log.info("Request {} passed validation. Отправка в DeepSeek. Topic: {}, Data: {}",
                    requestId, KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, request);

            try {
                kafkaTemplate.send(KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, requestId, request);
                log.info("Request {} успешно отправлен в Kafka. Topic: {}, Data: {}",
                        requestId, KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, request);
            } catch (Exception e) {
                log.error("Ошибка при отправке запроса {} в Kafka. Topic: {}, Data: {}",
                        requestId, KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, request, e);
            }
        } else {
            if (!isValid(request.getResumeText())) {
                log.warn("Request {} failed validation. Причина: некорректное поле 'ResumeText'. Data: {}",
                        requestId, request);
            }
            if (!isValid(request.getJobDescription())) {
                log.warn("Request {} failed validation. Причина: некорректное поле 'JobDescription'. Data: {}",
                        requestId, request);
            }
        }
    }

    private boolean isValid(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return text.toLowerCase().contains("опыт работы") ||
               text.toLowerCase().contains("навыки");
    }
}