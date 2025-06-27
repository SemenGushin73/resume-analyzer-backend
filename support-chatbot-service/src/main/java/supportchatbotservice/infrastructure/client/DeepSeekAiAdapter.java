package supportchatbotservice.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kata.project.chatbotDTO.AIRequestDTO;
import ru.kata.project.chatbotDTO.AIResponseDTO;
import supportchatbotservice.application.utils.PromptTemplates;
import supportchatbotservice.domain.ports.AiServicePort;

import java.util.Optional;

/**
 * Адаптер для работы с DeepSeek AI API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekAiAdapter implements AiServicePort {
    private final DeepSeekClient deepSeekClient;

    @Override
    @CircuitBreaker(name = "deepseek", fallbackMethod = "fallback")
    public Optional<String> getAiResponse(String message) {
        log.info("Отправка запроса к DeepSeek: {}", message);
        final String prompt = PromptTemplates.formatPrompt(message);

        final AIResponseDTO response = deepSeekClient.askAI(new AIRequestDTO(prompt, "session-default"));
        log.info("Получен ответ от DeepSeek: {}", response.getAnswer());
        return response.getAnswer();
    }

    public Optional<String> fallback(String userMessage, Throwable t) {
        log.warn("Fallback сработал из-за ошибки: {}", t.getMessage());
        return Optional.of("Извините, временно не могу обработать запрос. Попробуйте позже.");
    }
}