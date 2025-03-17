package webapp.deepseek.application.service;

import com.example.kafkadto.config.KafkaTopics;
import com.example.kafkadto.domain.model.ResumeDto;
import com.example.kafkadto.dto.AnalysisRequest;
import com.example.kafkadto.dto.AnalysisResult;
import com.example.kafkadto.dto.ResumeScoreAnalysisRequest;
import com.example.kafkadto.dto.ResumeScoreResult;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        log.info("processRequest: получено сообщение из топика: {}, requestId: {}, данные: {}",
                KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, requestId, request);

        long startTime = System.currentTimeMillis();

        try {
            String result = deepSeekService.analyzeResume(request.getResumeText(), request.getJobDescription());
            log.info("processRequest: Результат анализа для requestId {}: {}", requestId, result);

            kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_RESULTS, requestId, new AnalysisResult(requestId, result));
            log.info("processRequest: отправка результата в топик: {}, requestId: {}, результат: {}",
                    KafkaTopics.RESUME_ANALYZE_RESULTS, requestId, result);

            log.info("processRequest: запрос успешно обработан, requestId: {}", requestId);
        } catch (Exception e) {
            log.error("processRequest: Ошибка при обработке запроса {}: {}", requestId, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, groupId = "deepseek-analysis-group")
    public void consume(ResumeScoreAnalysisRequest request) {
        try {
            ResumeDto resumeDto = request.getResume();
            String resumeText = resumeDto.toString();
            String analysis = deepSeekService.analyzeResumeScore(resumeText);

            ResumeScoreResult result = mapper.readValue(analysis, ResumeScoreResult.class);
            result.setResumeId(request.getResumeId());

            kafkaScoreTemplate.send(KafkaTopics.AI_RESUME_SCORE_RESULTS, result);

            log.info("Отправлен результат анализа резюме {}", request.getResumeId());
        } catch (Exception e) {
            log.error("Ошибка анализа резюме: ", e);
        }
    }
}
