package supportchatbotservice.domain.ports;

import java.util.Optional;

/**
 * Порт для взаимодействия с внешним AI-сервисом.
 */
public interface AiServicePort {
    Optional<String> getAiResponse(String message);
}