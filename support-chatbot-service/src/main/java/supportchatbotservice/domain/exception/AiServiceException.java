package supportchatbotservice.domain.exception;

/**
 * Исключение при ошибках во внешнем AI-сервисе.
 */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }
}