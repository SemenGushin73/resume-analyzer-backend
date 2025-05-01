package webapp.deepseek.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import webapp.deepseek.utils.ai.AIMessageSender;
import webapp.deepseek.utils.ai.PromptGenerator;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResumeAnalysisTest {

    @Mock
    private AIMessageSender aiMessageSender;

    @Mock
    private PromptGenerator promptGenerator;

    @InjectMocks
    private AIServiceImpl service;

    private static final String RESUME_TEXT = "Test resume";
    private static final String JOB_DESC = "Test job";

    @Test
    void analyzeResume_ShouldReturnSuccessResult() {
        final String expectedResponse = "AI analysis result";
        when(promptGenerator.createPrompt(anyString(), anyString()))
                .thenReturn("Generated prompt");
        when(aiMessageSender.send(anyString()))
                .thenReturn(expectedResponse);

        final String result = service.analyzeResume(RESUME_TEXT, JOB_DESC);

        assertEquals(expectedResponse, result);
    }

    @Test
    void analyzeResume_ShouldHandleApiErrors() {
        when(promptGenerator.createPrompt(anyString(), anyString()))
                .thenReturn("Generated prompt");
        when(aiMessageSender.send(anyString()))
                .thenThrow(new RuntimeException("API error"));

        final String result = service.analyzeResume(RESUME_TEXT, JOB_DESC);

        assertTrue(result.contains("Ошибка анализа: API error"));
    }

    private static Stream<Arguments> errorResponses() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("   ")
        );
    }

    @ParameterizedTest
    @MethodSource("errorResponses")
    void analyzeResume_ShouldReturnErrorMessage_WhenInvalidResponse(String invalidResponse) {
        when(promptGenerator.createPrompt(anyString(), anyString()))
                .thenReturn("Generated prompt");
        when(aiMessageSender.send(anyString()))
                .thenReturn(invalidResponse);

        final String result = service.analyzeResume(RESUME_TEXT, JOB_DESC);

        assertEquals("Не удалось проанализировать резюме.", result);
    }
}