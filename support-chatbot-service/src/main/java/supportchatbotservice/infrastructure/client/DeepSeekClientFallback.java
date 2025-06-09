package supportchatbotservice.infrastructure.client;


import org.springframework.stereotype.Component;
import supportchatbotservice.application.dto.DeepSeekRequestDTO;
import supportchatbotservice.application.dto.DeepSeekResponseDTO;

/**
 * Fallback-реализация для DeepSeek клиента.
 */
@Component
public class DeepSeekClientFallback implements DeepSeekClient {
    @Override
    public DeepSeekResponseDTO askAI(DeepSeekRequestDTO request) {
        return new DeepSeekResponseDTO("Fallback response: Service unavailable");
    }
}