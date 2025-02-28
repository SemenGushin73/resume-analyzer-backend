package webapp.deepseek.application.service;

import com.example.kafkadto.config.KafkaTopics;
import com.example.kafkadto.dto.AnalysisRequest;
import com.example.kafkadto.dto.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import webapp.deepseek.domain.service.DeepSeekService;

@Service
@Slf4j
public class KafkaConsumerService {

    private final DeepSeekService deepSeekService;
    private final KafkaTemplate<String, AnalysisResult> kafkaTemplate;

    public KafkaConsumerService(DeepSeekService deepSeekService, KafkaTemplate<String, AnalysisResult> kafkaTemplate) {
        this.deepSeekService = deepSeekService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS)
    public void processRequest(@Header("kafka_receivedMessageKey") String requestId, AnalysisRequest request) {
        log.info("processRequest: запрос по id: {}", requestId);
        try {
            String result = deepSeekService.analyzeResume(request.getResumeText(), request.getJobDescription());
            log.info("processRequest: Результат анализа: {}", result);
            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_RESULTS, requestId, new AnalysisResult(requestId, result));
            log.info("processRequest: отправка запроса в кафка: {}", requestId);
        } catch (Exception e) {
            log.error("processRequest: Error processing request {}: {}", requestId, e.getMessage(), e);
        }
    }
}