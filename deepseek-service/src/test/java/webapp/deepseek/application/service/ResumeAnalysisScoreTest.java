package webapp.deepseek.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import webapp.deepseek.domain.service.AIService;
import webapp.deepseek.infrastructure.client.OpenRouterApiClient;
import webapp.deepseek.infrastructure.client.OpenRouterResponse;
import webapp.deepseek.utils.TestDataBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResumeAnalysisScoreTest {

    @Mock
    OpenRouterApiClient apiClient;

    @InjectMocks
    AIService service;

    private static final String RESUME_TEXT = "Test resume";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", "test_key");
        ReflectionTestUtils.setField(service, "aiModel", "test_model");
    }

    @Test
    void analyzeResumeScore_ShouldReturnSuccessResult() {
        final OpenRouterResponse response = TestDataBuilder.createMockResponse("AI resume score");
        when(apiClient.generateCompletion(any(), any(), any())).
                thenReturn(response);

        final String result = service.analyzeResumeScore(RESUME_TEXT);

        assertEquals("AI resume score", result);
    }

    private static Stream<Arguments> errorResponses() {
        final OpenRouterResponse responseWithEmptyChoices = new OpenRouterResponse();
        responseWithEmptyChoices.setChoices(List.of());

        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(new OpenRouterResponse()),
                Arguments.of(responseWithEmptyChoices)
        );
    }

    @ParameterizedTest
    @MethodSource("errorResponses")
    void analyzeResumeScore_ShouldReturnErrorMessage_WhenInvalidResponse(OpenRouterResponse response) {
        when(apiClient.generateCompletion(any(), any(), any()))
                .thenReturn(response);

        final Exception exception = assertThrows(RuntimeException.class, () -> service.analyzeResumeScore(RESUME_TEXT));
        assertEquals("Ошибка: AI не смог обработать запрос.", exception.getMessage());
    }
}
