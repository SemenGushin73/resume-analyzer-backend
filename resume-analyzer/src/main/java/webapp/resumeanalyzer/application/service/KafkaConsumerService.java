package webapp.resumeanalyzer.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.AnalysisResult;
import ru.kata.project.kafka.ResumeScoreAnalysisRequest;
import ru.kata.project.kafka.ResumeScoreRequest;
import ru.kata.project.kafka.ResumeScoreResult;
import ru.kata.project.kafka.config.KafkaTopics;
import webapp.resumeanalyzer.application.mapper.ResumeMapper;
import webapp.resumeanalyzer.application.utils.ai.ValidResumeChecker;
import webapp.resumeanalyzer.domain.model.Resume;
import webapp.resumeanalyzer.domain.service.ResumeService;

/**
 * Прослушивание и получение запросов из kafka.
 */
@Service
@Slf4j
public class KafkaConsumerService {

    private final KafkaTemplate<String, AnalysisResult> kafkaTemplate;
    private final KafkaTemplate<String, ResumeScoreResult> kafkaScoreTemplate;
    private final ResumeService resumeService;
    private final ResumeMapper resumeMapper;
    private final AIService aiService;
    private final ValidResumeChecker validResumeChecker;
    private final ObjectMapper mapper;

    @Autowired
    public KafkaConsumerService(KafkaTemplate<String, AnalysisResult> kafkaTemplate,
                              KafkaTemplate<String, ResumeScoreResult> kafkaScoreTemplate, 
                              ResumeService resumeService,
                              ResumeMapper resumeMapper, 
                              AIService aiService,
                              ValidResumeChecker validResumeChecker,
                              ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaScoreTemplate = kafkaScoreTemplate;
        this.resumeService = resumeService;
        this.resumeMapper = resumeMapper;
        this.aiService = aiService;
        this.validResumeChecker = validResumeChecker;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_REQUESTS)
    public void AnalysisRequest(@Header("kafka_receivedMessageKey") String requestId, AnalysisRequest request) {
        log.info("Получен запрос для анализа. Topic: {}, requestId: {}, данные: {}",
                KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);

        if (validResumeChecker.isValidResumeAndJobDescribtion(request.getResumeText(), request.getJobDescription())) {
            log.info("Request {} passed validation. Отправка в DeepSeek. Topic: {}, Data: {}",
                    requestId, KafkaTopics.RESUME_ANALYZE_RESULTS, request);

            try {
                final String result = aiService.analyzeResume(request.getResumeText(), request.getJobDescription());
                kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_RESULTS, requestId, new AnalysisResult(requestId, result));
                log.info("Request {} успешно отправлен в Kafka. Topic: {}, Data: {}, Result: {}",
                        requestId, KafkaTopics.RESUME_ANALYZE_RESULTS, request, result);
            } catch (Exception e) {
                log.error("Ошибка при отправке запроса {} в Kafka. Topic: {}, Data: {}",
                        requestId, KafkaTopics.RESUME_ANALYZE_RESULTS, request, e);
            }
        } else {
            log.warn("Request {} failed validation. Причина: некорректное поле 'ResumeText' или 'JobDescription'. Data: {}",
                    requestId, request);
        }
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.AI_RESUME_SCORE_REQUESTS, groupId = "resume-score-group")
    public void consume(ResumeScoreRequest request) {
        try {
            // Получаем резюме с инициализированными коллекциями
            Resume resume = resumeService.getResumeById(request.getResumeId());
            
            // Преобразуем в DTO внутри транзакции
            ResumeScoreAnalysisRequest resumeScoreAnalysisRequest = new ResumeScoreAnalysisRequest(
                    resume.getId().toString(),
                    resumeMapper.toDto(resume)
            );

            final String analysis = aiService.analyzeResumeScore(resumeScoreAnalysisRequest.getResume().toString());
            final ResumeScoreResult result = mapper.readValue(analysis, ResumeScoreResult.class);
            result.setResumeId(request.getResumeId());

            kafkaScoreTemplate.send(KafkaTopics.AI_RESUME_SCORE_RESULTS, result);
            log.info("Отправлен запрос на анализ резюме {} в DeepSeek-service", request.getResumeId());
        } catch (Exception e) {
            log.error("Ошибка обработки запроса: ", e);
            throw new RuntimeException("Ошибка обработки запроса оценки резюме", e);
        }
    }
}