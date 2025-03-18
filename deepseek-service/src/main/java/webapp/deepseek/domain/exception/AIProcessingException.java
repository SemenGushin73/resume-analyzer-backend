package webapp.deepseek.domain.exception;

/**
 * Класс - исключение для взаимодействия с AI
 */
public class AIProcessingException extends RuntimeException {
    public AIProcessingException(String message) {
        super(message);
    }

    public AIProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
