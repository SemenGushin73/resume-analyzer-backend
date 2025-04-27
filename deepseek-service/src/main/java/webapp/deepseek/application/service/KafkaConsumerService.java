package webapp.deepseek.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.kata.project.kafka.*;
import ru.kata.project.kafka.config.KafkaTopics;
import webapp.deepseek.domain.service.DeepSeekService;

@Service
@Slf4j
public class KafkaConsumerService {

    private final DeepSeekService deepSeekService;
    private final KafkaTemplate<String, AnalysisResult> kafkaTemplate;
    private final KafkaTemplate<String, ResumeScoreResult> kafkaScoreTemplate;
    private final ObjectMapper mapper;

    public KafkaConsumerService(DeepSeekService deepSeekService,
                                KafkaTemplate<String, AnalysisResult> kafkaTemplate,
                                KafkaTemplate<String, ResumeScoreResult> kafkaScoreTemplate,
                                ObjectMapper mapper) {
        this.deepSeekService = deepSeekService;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaScoreTemplate = kafkaScoreTemplate;
        this.mapper = mapper;
    }

    @KafkaListener(topics = KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS)
    public void processRequest(@Header("kafka_receivedMessageKey") String requestId, AnalysisRequest request) {
        log.info("Получено сообщение: requestId={}, данные={}", requestId, request);
        try {
            final String result = deepSeekService.analyzeResume(request.getResumeText(), request.getJobDescription());
            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_RESULTS, requestId, new AnalysisResult(requestId, result));
            log.info("Результат анализа отправлен: requestId={}, результат={}", requestId, result);
        } catch (Exception e) {
            log.error("Ошибка обработки запроса {}: {}", requestId, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.DEEPSEEK_RESUME_SCORE_REQUESTS, groupId = "deepseek-analysis-group")
    public void consume(ResumeScoreAnalysisRequest request) {
        try {
            final String analysis = deepSeekService.analyzeResumeScore(request.getResume().toString());
            final ResumeScoreResult result = mapper.readValue(analysis, ResumeScoreResult.class);
            result.setResumeId(request.getResumeId());
            kafkaScoreTemplate.send(KafkaTopics.AI_RESUME_SCORE_RESULTS, result);
            log.info("Результат оценки отправлен: resumeId={}", request.getResumeId());
        } catch (Exception e) {
            log.error("Ошибка оценки резюме: ", e);
        }
    }
}
