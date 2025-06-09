package supportchatbotservice.domain.ports;

/**
 * Порт для взаимодействия с внешним AI-сервисом.
 */
public interface AiServicePort {
    String getAiResponse(String message);
}