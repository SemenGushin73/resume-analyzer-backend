package webapp.resumeanalyzer.application.service;

import com.example.kafkadto.config.KafkaTopics;
import com.example.kafkadto.dto.AnalysisRequest;
import com.example.kafkadto.dto.ResumeScoreAnalysisRequest;
import com.example.kafkadto.dto.ResumeScoreRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;

import org.springframework.stereotype.Service;
import webapp.resumeanalyzer.application.mapper.ResumeMapper;
import webapp.resumeanalyzer.domain.model.Resume;
import webapp.resumeanalyzer.domain.service.ResumeService;


@Service
@Slf4j
public class KafkaConsumerService {

    private final KafkaTemplate<String, AnalysisRequest> kafkaTemplate;
    private final KafkaTemplate<String, ResumeScoreAnalysisRequest> kafkaScoreTemplate;
    private final ResumeService resumeService;
    private final ResumeMapper resumeMapper;

    @Value("${kafka.topics.deepseek-analysis-requests}")
    private String deepseekTopic;

    @Autowired
    public KafkaConsumerService(KafkaTemplate<String, AnalysisRequest> kafkaTemplate,
                                KafkaTemplate<String, ResumeScoreAnalysisRequest> kafkaScoreTemplate,
                                ResumeService resumeService,
                                ResumeMapper resumeMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaScoreTemplate = kafkaScoreTemplate;
        this.resumeService = resumeService;
        this.resumeMapper = resumeMapper;
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

    @KafkaListener(topics = KafkaTopics.AI_RESUME_SCORE_REQUESTS, groupId = "resume-score-group")
    public void consume(ResumeScoreRequest request) {
        try {
            Resume resume = resumeService.getResumeById(request.getResumeId());
            ResumeScoreAnalysisRequest analysisScoreRequest = new ResumeScoreAnalysisRequest(
                    resume.getId().toString(),
                    resumeMapper.toDto(resume)
            );

            kafkaScoreTemplate.send(deepseekTopic, analysisScoreRequest);
            log.info("Отправлен запрос на анализ резюме {} в DeepSeek-service", request.getResumeId());
        } catch (Exception e) {
            log.error("Ошибка обработки запроса: ", e);
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