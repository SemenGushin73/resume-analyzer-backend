package webapp.deepseek.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PromptGenerationTest {

    @InjectMocks
    DeepSeekServiceImpl service;

    private static final String RESUME_TEXT = "Test resume";
    private static final String JOB_DESCRIPTION = "Test job";
    private static final String TEMPLATE = "Test template";
    private static final String DEPT_DESC = "Test department";

    @Test
    void createPrompt_ShouldIncludeResumeAndJobDescription() {
        final String prompt = ReflectionTestUtils.invokeMethod(service, "createPrompt", RESUME_TEXT, JOB_DESCRIPTION);

        assertThat(prompt)
                .startsWith("Вы полезный помощник")
                .contains("Resume:\n" + RESUME_TEXT)
                .contains("Job Description:\n" + JOB_DESCRIPTION);
    }

    @Test
    void createPromptForGeneration_ShouldIncludeTemplateAndDepartment() {
        final String prompt = ReflectionTestUtils.invokeMethod(service, "createPromptForGeneration", TEMPLATE, DEPT_DESC);

        assertThat(prompt)
                .startsWith("Напиши профессиональное")
                .contains(TEMPLATE)
                .contains(DEPT_DESC);
    }

    @Test
    void createPromptForScoring_ShouldIncludeResumeScore() {
        final String prompt = ReflectionTestUtils.invokeMethod(service, "createPromptForScoring", RESUME_TEXT);

        assertThat(prompt)
                .startsWith("Руководствуясь параметрами ниже оцени резюме")
                .contains(RESUME_TEXT);
    }
}
