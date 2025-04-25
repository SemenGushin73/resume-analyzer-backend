package webapp.deepseekservice.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kata.project.kafka.ResumeScoreAnalysisRequest;
import ru.kata.project.kafka.ResumeScoreResult;
import ru.kata.project.kafka.config.KafkaTopics;
import ru.kata.project.resumeDto.ResumeDto;
import webapp.deepseek.application.service.DeepSeekServiceImpl;
import webapp.deepseek.application.service.KafkaConsumerService;
import webapp.deepseekservice.utils.TestDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerServiceConsumeTest {

    @Mock
    private KafkaTemplate<String, ResumeScoreResult> kafkaScoreTemplate;

    @Mock
    private DeepSeekServiceImpl deepSeekService;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private ResumeScoreAnalysisRequest request;
    private ResumeScoreResult mockResult;
    private static final String RESUME_ID = "123";
    private static final String DEEPSEEK_RESPONSE_JSON = """
            {
              "score": 90,
              "feedback": ["Хорошее резюме"]
            }
            """;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ResumeDto resume = TestDataBuilder.createSampleResume();
        request = new ResumeScoreAnalysisRequest(RESUME_ID, resume);
        mockResult = new ResumeScoreResult(UUID.randomUUID().toString(), 90,
                List.of("Хорошее резюме"));

        lenient().when(deepSeekService.analyzeResumeScore(any()))
                .thenReturn(DEEPSEEK_RESPONSE_JSON);
        lenient().when(mapper.readValue(anyString(), eq(ResumeScoreResult.class)))
                .thenReturn(mockResult);
    }

    @Test
    void consume_ShouldSetResumeId_WhenProcessingSuccess() {
        kafkaConsumerService.consume(request);

        ArgumentCaptor<ResumeScoreResult> captor = ArgumentCaptor.forClass(ResumeScoreResult.class);
        verify(kafkaScoreTemplate).send(any(), captor.capture());

        ResumeScoreResult sentResult = captor.getValue();

        assertAll(
                () -> assertEquals(RESUME_ID, sentResult.getResumeId()),
                () -> assertEquals(90, sentResult.getScore()),
                () -> assertEquals(List.of("Хорошее резюме"), sentResult.getFeedback())
        );
    }

    @Test
    void consume_ShouldSendScoreResult_WhenValidRequest() throws JsonProcessingException {
        kafkaConsumerService.consume(request);

        verify(deepSeekService).analyzeResumeScore(request.getResume().toString());
        verify(mapper).readValue(anyString(), eq(ResumeScoreResult.class));
        verify(kafkaScoreTemplate).send(eq(KafkaTopics.AI_RESUME_SCORE_RESULTS),
                any(ResumeScoreResult.class));
    }

    @Test
    void consume_ShouldNotSendResult_WhenJsonParsingFails() throws JsonProcessingException {
        when(mapper.readValue(anyString(), eq(ResumeScoreResult.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {
                });

        kafkaConsumerService.consume(request);

        verify(kafkaScoreTemplate, never()).send(any(), any());
    }

    @Test
    void consume_ShouldHandleEmptyResume_WhenAnalysisFails() throws JsonProcessingException {
        ResumeDto emptyResumeDto = new ResumeDto();
        ResumeScoreAnalysisRequest emptyRequest = new ResumeScoreAnalysisRequest(RESUME_ID, emptyResumeDto);
        when(deepSeekService.analyzeResumeScore(any()))
                .thenThrow(new RuntimeException("Ошибка: AI не смог обработать запрос."));

        kafkaConsumerService.consume(emptyRequest);

        verify(mapper, never()).readValue(anyString(), eq(ResumeScoreResult.class));
        verify(kafkaScoreTemplate, never()).send(any(), any());
    }

    @Test
    void consume_ShouldHandleNullRequest() {
        kafkaConsumerService.consume(null);
        verifyNoInteractions(deepSeekService, mapper, kafkaScoreTemplate);
    }

    @Test
    void consume_ShouldCatchException_WhenKafkaSendFails() {
        when(kafkaScoreTemplate.send(any(), any())).thenThrow(new RuntimeException("Kafka error"));
        assertDoesNotThrow(() -> kafkaConsumerService.consume(request));
    }
}
