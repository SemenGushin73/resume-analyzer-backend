package webapp.deepseek.application.service.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.AnalysisResult;
import ru.kata.project.kafka.config.KafkaTopics;
import webapp.deepseek.application.service.DeepSeekServiceImpl;
import webapp.deepseek.application.service.KafkaConsumerService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerServiceProcessRequestTest {

    @Mock
    private KafkaTemplate<String, AnalysisResult> kafkaTemplate;

    @Mock
    private DeepSeekServiceImpl deepSeekService;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private static final String REQUEST_ID = "123";
    private final AnalysisRequest validRequest = new AnalysisRequest("testResume", "testJobDesc");

    @Test
    void processRequest_ShouldCorrectlyParseAndProcessValidRequest() {
        when(deepSeekService.analyzeResume(validRequest.getResumeText(), validRequest.getJobDescription()))
                .thenReturn("OK");

        kafkaConsumerService.processRequest(REQUEST_ID, validRequest);

        verify(deepSeekService).analyzeResume("testResume", "testJobDesc");
        verify(kafkaTemplate).send(eq(KafkaTopics.RESUME_ANALYZE_RESULTS), eq("123"),
                eq(new AnalysisResult("123", "OK")));
    }

    @Test
    void processRequest_ShouldHandleDeepSeekErrors_Gracefully() {
        when(deepSeekService.analyzeResume(any(), any()))
                .thenThrow(new RuntimeException("API error"));

        kafkaConsumerService.processRequest(REQUEST_ID, validRequest);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequests")
    void processRequest_ShouldRejectInvalidRequests(AnalysisRequest invalidRequest) {
        when(deepSeekService.analyzeResume(any(), any()))
                .thenThrow(new RuntimeException("AnalysisRequest is null"));

        kafkaConsumerService.processRequest(REQUEST_ID, invalidRequest);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    private static Stream<Arguments> provideInvalidRequests() {
        return Stream.of(
                Arguments.of(new AnalysisRequest(null, "valid_job")),
                Arguments.of(new AnalysisRequest("", "valid_job")),
                Arguments.of(new AnalysisRequest("   ", "valid_job")),
                Arguments.of(new AnalysisRequest("valid_resume", null))
        );
    }

    @Test
    void processRequest_ShouldHandleKafkaSendExceptionGracefully() {
        when(deepSeekService.analyzeResume(any(), any()))
                .thenReturn("OK");
        when(kafkaTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("Kafka send failed"));

        assertDoesNotThrow(() ->
                kafkaConsumerService.processRequest(REQUEST_ID, validRequest)
        );
    }
}
