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
import webapp.deepseek.utils.ai.AIMessageSender;
import webapp.deepseek.utils.ai.PromptGenerator;
import webapp.deepseek.utils.TestDataBuilder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResumeGenerationTest {

    @Mock
    private AIMessageSender aiMessageSender;

    @Mock
    private PromptGenerator promptGenerator;

    @InjectMocks
    private AIServiceImpl service;

    private static final String TEMPLATE = "Test template";
    private static final String DEPARTMENT_DESCRIPTION = "Test departmentDescription";

    @Test
    void generateResume_ShouldReturnFormattedResume() {
        final String expectedResponse = "Generated resume";
        when(promptGenerator.createPromptForGeneration(anyString(), anyString()))
                .thenReturn("Generated prompt");
        when(aiMessageSender.send(anyString()))
                .thenReturn(expectedResponse);

        final String result = service.generateResume(TEMPLATE, DEPARTMENT_DESCRIPTION);

        assertEquals(expectedResponse, result);
    }

    @Test
    void generateResume_ShouldHandleApiErrors() {
        when(promptGenerator.createPromptForGeneration(anyString(), anyString()))
                .thenReturn("Generated prompt");
        when(aiMessageSender.send(anyString()))
                .thenThrow(new RuntimeException("API error"));

        final Exception exception = assertThrows(RuntimeException.class, 
            () -> service.generateResume(TEMPLATE, DEPARTMENT_DESCRIPTION));
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
    void generateResume_ShouldHandleInvalidResponses(String invalidResponse) {
        when(promptGenerator.createPromptForGeneration(anyString(), anyString()))
                .thenReturn("Generated prompt");
        when(aiMessageSender.send(anyString()))
                .thenReturn(invalidResponse);

        final Exception exception = assertThrows(RuntimeException.class,
            () -> service.generateResume(TEMPLATE, DEPARTMENT_DESCRIPTION));
        assertEquals("Ошибка: AI не смог обработать запрос.", exception.getMessage());
    }
}