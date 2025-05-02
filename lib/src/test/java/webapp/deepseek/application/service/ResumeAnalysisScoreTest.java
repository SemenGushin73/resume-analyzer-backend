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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResumeAnalysisScoreTest {

    @Mock
    private AIMessageSender aiMessageSender;

    @Mock
    private PromptGenerator promptGenerator;

    @InjectMocks
    private AIServiceImpl service;

    private static final String RESUME_TEXT = "Test resume";

    @Test
    void analyzeResumeScore_ShouldReturnSuccessResult() {
        final String expectedResponse = "AI resume score";
        when(promptGenerator.createPromptForScoring(anyString()))
                .thenReturn("Generated scoring prompt");
        when(aiMessageSender.send(anyString()))
                .thenReturn(expectedResponse);

        final String result = service.analyzeResumeScore(RESUME_TEXT);

        assertEquals(expectedResponse, result);
    }

    @Test
    void analyzeResumeScore_ShouldHandleApiErrors() {
        when(promptGenerator.createPromptForScoring(anyString()))
                .thenReturn("Generated scoring prompt");
        when(aiMessageSender.send(anyString()))
                .thenThrow(new RuntimeException("API error"));

        final Exception exception = assertThrows(RuntimeException.class, 
            () -> service.analyzeResumeScore(RESUME_TEXT));
        assertEquals("Ошибка: AI не смог обработать запрос.", exception.getMessage());
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
    void analyzeResumeScore_ShouldThrowException_WhenInvalidResponse(String invalidResponse) {
        when(promptGenerator.createPromptForScoring(anyString()))
                .thenReturn("Generated scoring prompt");
        when(aiMessageSender.send(anyString()))
                .thenReturn(invalidResponse);

        final Exception exception = assertThrows(RuntimeException.class,
            () -> service.analyzeResumeScore(RESUME_TEXT));
        assertEquals("Ошибка: AI не смог обработать запрос.", exception.getMessage());
    }
}