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
import webapp.deepseek.infrastructure.client.OpenRouterApiClient;
import webapp.deepseek.infrastructure.client.OpenRouterResponse;
import webapp.deepseek.utils.TestDataBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResumeGenerationTest {

    @Mock
    OpenRouterApiClient apiClient;

    @InjectMocks
    DeepSeekServiceImpl service;

    private static final String TEMPLATE = "Test template";
    private static final String DEPARTMENT_DESCRIPTION = "Test departmentDescription";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", "test_key");
        ReflectionTestUtils.setField(service, "aiModel", "test_model");
    }

    @Test
    void generateResume_ShouldReturnFormattedResume() {
        final OpenRouterResponse response = TestDataBuilder.createMockResponse("Generated resume");
        when(apiClient.generateCompletion(any(), any(), any()))
                .thenReturn(response);

        final String result = service.generateResume(TEMPLATE, DEPARTMENT_DESCRIPTION);

        assertEquals("Generated resume", result);
    }

    @Test
    void generateResume_ShouldHandleApiErrors() {
        when(apiClient.generateCompletion(any(), any(), any()))
                .thenThrow(new RuntimeException("API error"));

        final String result = service.generateResume(TEMPLATE, DEPARTMENT_DESCRIPTION);

        assertTrue(result.contains("Ошибка генерации: API error"));
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
    void generateResume_ShouldHandleInvalidResponses(OpenRouterResponse response) {
        when(apiClient.generateCompletion(any(), any(), any()))
                .thenReturn(response);

        final String result = service.generateResume(TEMPLATE, DEPARTMENT_DESCRIPTION);

        assertEquals("Не удалось сгенерировать резюме.", result);
    }
}
