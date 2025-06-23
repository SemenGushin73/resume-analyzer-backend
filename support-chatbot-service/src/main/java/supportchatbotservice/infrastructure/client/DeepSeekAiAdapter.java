package supportchatbotservice.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import supportchatbotservice.application.dto.DeepSeekResponseDTO;
import supportchatbotservice.application.utils.PromptTemplates;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.application.dto.DeepSeekRequestDTO;

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
    public String getAiResponse(String message) {
        log.info("Отправка запроса к DeepSeek: {}", message);
        String prompt = PromptTemplates.formatPrompt(message);

        DeepSeekResponseDTO response = deepSeekClient.askAI(new DeepSeekRequestDTO(prompt));
        log.info("Получен ответ от DeepSeek: {}", response.getAnswer());
        return response.getAnswer();
    }

    private String fallback(String message, Exception ex) {
        log.error("Ошибка при запросе к DeepSeek: {}", ex.getMessage());
        return "Сервис временно недоступен";
    }
}