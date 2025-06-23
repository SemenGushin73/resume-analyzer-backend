package supportchatbotservice.infrastructure.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import supportchatbotservice.application.dto.DeepSeekRequestDTO;
import supportchatbotservice.application.dto.DeepSeekResponseDTO;


/**
 * Fallback-реализация для DeepSeek клиента.
 */
@Slf4j
@Component
public class DeepSeekClientFallback implements DeepSeekClient {

    @Override
    public DeepSeekResponseDTO askAI(DeepSeekRequestDTO request) {
        log.warn("⚠️ Fallback: DeepSeek API недоступен");

        DeepSeekResponseDTO fallbackResponse = new DeepSeekResponseDTO();

        return fallbackResponse;
    }
}