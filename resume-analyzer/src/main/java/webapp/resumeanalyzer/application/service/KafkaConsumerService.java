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
        if (isValid(request.getResumeText()) && isValid(request.getJobDescription())) {
            log.info("Request {} passed validation. Отправка в DeepSeek.", requestId);
            kafkaTemplate.send(KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, requestId, request);
        } else {
            log.warn("Request {} failed validation.", requestId);
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