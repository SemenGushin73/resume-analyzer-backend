package supportchatbotservice.infrastructure.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.kata.project.chatbotDTO.AIRequestDTO;
import ru.kata.project.chatbotDTO.AIResponseDTO;


/**
 * Fallback-реализация для DeepSeek клиента.
 */
@Slf4j
@Component
public class DeepSeekClientFallback implements DeepSeekClient {

    @Override
    public AIResponseDTO askAI(AIRequestDTO request) {
        log.warn("⚠️ Fallback: DeepSeek API недоступен");

        final AIResponseDTO fallbackResponse = new AIResponseDTO();

        return fallbackResponse;
    }
}