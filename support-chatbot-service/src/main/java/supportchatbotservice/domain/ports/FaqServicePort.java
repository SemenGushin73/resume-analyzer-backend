package supportchatbotservice.domain.ports;

import java.util.Optional;

/**
 * Порт для работы с FAQ-сервисом.
 */
public interface FaqServicePort {
    Optional<String> findAnswer(String question);
}