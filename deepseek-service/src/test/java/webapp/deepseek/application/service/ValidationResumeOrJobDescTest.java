package webapp.deepseek.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import webapp.deepseek.utils.ai.ValidResumeChecker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ValidationResumeOrJobDescTest {

    private ValidResumeChecker service;

    @ParameterizedTest
    @ValueSource(strings = {
            "опыт работы",
            "НАВЫКИ программирования",
            "Образование: высшее",
            "должностные обязанности инженера",
            "Требования к кандидату",
            "Моё резюме",
            "Вакансия: разработчик"
    })
    void isValidResumeOrJobDescription_ShouldReturnTrue_WhenValidText(String text) {
        assertTrue(service.isValidResumeOrJobDescription(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void isValidResumeOrJobDescription_ShouldReturnFalse_WhenEmptyOrBlank(String text) {
        assertFalse(service.isValidResumeOrJobDescription(text));
    }

    @Test
    void isValidResumeOrJobDescription_ShouldReturnFalse_WhenNoKeywords() {
        assertFalse(service.isValidResumeOrJobDescription("просто случайный текст"));
    }
}
