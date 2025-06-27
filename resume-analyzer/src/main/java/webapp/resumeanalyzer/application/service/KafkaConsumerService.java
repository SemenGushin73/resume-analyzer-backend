package webapp.resumeanalyzer.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

import java.io.IOException;

/**
 * Прослушивание и получение запросов из kafka.
 */
@Service
@Slf4j
public class KafkaConsumerService {

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final KafkaTemplate<String, AnalysisResult> kafkaTemplate;
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final KafkaTemplate<String, ResumeScoreResult> kafkaScoreTemplate;
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final ResumeService resumeService;
    private final ResumeMapper resumeMapper;
    private final AIService aiService;
    private final ValidResumeChecker validResumeChecker;
    @SuppressFBWarnings("EI_EXPOSE_REP2")
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
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_REQUESTS)
    public void analysisRequest(@Header("kafka_receivedMessageKey") String requestId, AnalysisRequest request) {
        final String sanitizedRequestId = sanitizeLogInput(requestId);
        final String sanitizedRequestToString = sanitizeLogInput(request.toString());
        final String sanitizedResumeText = sanitizeLogInput(request.getResumeText());
        final String sanitizedJobDescription = sanitizeLogInput(request.getJobDescription());

        log.info("Получен запрос для анализа. Topic: {}, requestId: {}, данные: {}",
                KafkaTopics.RESUME_ANALYZE_REQUESTS, sanitizedRequestId, sanitizedRequestToString);

        if (validResumeChecker.isValidResumeAndJobDescribtion(sanitizedResumeText, sanitizedJobDescription)) {
            log.info("Request {} passed validation. Отправка в DeepSeek. Topic: {}, Data: {}",
                    sanitizedRequestId, KafkaTopics.RESUME_ANALYZE_RESULTS, sanitizedRequestToString);

            try {
                final String result = aiService.analyzeResume(sanitizedResumeText, sanitizedJobDescription);
                final String sanitizedResult = sanitizeLogInput(result);
                kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_RESULTS, requestId, new AnalysisResult(requestId, result));
                log.info("Request {} успешно отправлен в Kafka. Topic: {}, Data: {}, Result: {}",
                        sanitizedRequestId, KafkaTopics.RESUME_ANALYZE_RESULTS, sanitizedRequestToString, sanitizedResult);
            } catch (Exception e) {
                final String sanitizedErrorMessage = sanitizeLogInput(e.getMessage());
                log.error("Ошибка при отправке запроса {} в Kafka. Topic: {}, Data: {}. Сообщение об ошибке: {}",
                        sanitizedRequestId, KafkaTopics.RESUME_ANALYZE_RESULTS, sanitizedRequestToString, sanitizedErrorMessage, e);
            }
        } else {
            log.warn("Request {} failed validation. Причина: некорректное поле 'ResumeText' или 'JobDescription'. Data: {}",
                    sanitizedRequestId, sanitizedRequestToString);
        }
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.AI_RESUME_SCORE_REQUESTS, groupId = "resume-score-group")
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    public void consume(ResumeScoreRequest request) {
        final String sanitizedResumeId = sanitizeLogInput(request.getResumeId());
        try {
            final Resume resume = resumeService.getResumeById(sanitizedResumeId);
            final ResumeScoreAnalysisRequest resumeScoreAnalysisRequest = new ResumeScoreAnalysisRequest(
                    resume.getId().toString(),
                    resumeMapper.toDto(resume)
            );

            final String analysis = aiService.analyzeResumeScore(
                    sanitizeLogInput(resumeScoreAnalysisRequest.getResume().toString()));
            final ResumeScoreResult result = mapper.readValue(analysis, ResumeScoreResult.class);
            result.setResumeId(request.getResumeId());

            kafkaScoreTemplate.send(KafkaTopics.AI_RESUME_SCORE_RESULTS, result);
            log.info("Отправлен запрос на анализ резюме {} в DeepSeek-service", sanitizedResumeId);
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации ответа AI для запроса оценки резюме {}: {}",
                    sanitizedResumeId, sanitizeLogInput(e.getMessage()), e);
            throw new RuntimeException("Ошибка десериализации ответа AI для запроса оценки резюме", e);
        } catch (EmptyResultDataAccessException e) {
            log.error("Резюме с ID {} не найдено при обработке запроса оценки: {}",
                    sanitizedResumeId, sanitizeLogInput(e.getMessage()), e);
            throw new RuntimeException("Резюме не найдено для оценки", e);
        } catch (RuntimeException e) {
            log.error("Непредвиденная ошибка времени выполнения при обработке запроса оценки резюме {}: {}",
                    sanitizedResumeId, sanitizeLogInput(e.getMessage()), e);
            throw new RuntimeException("Непредвиденная ошибка обработки запроса оценки резюме", e);
        } catch (Exception e) {
            log.error("Неизвестная ошибка при обработке запроса оценки резюме {}: {}",
                    sanitizedResumeId, sanitizeLogInput(e.getMessage()), e);
            throw new RuntimeException("Неизвестная ошибка обработки запроса оценки резюме", e);
        }
    }

    private String sanitizeLogInput(String input) {
        if (input == null) {
            return "null";
        }
        return input.replace('\r', ' ').replace('\n', ' ');
    }
}