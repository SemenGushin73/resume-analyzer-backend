package supportchatbotservice.domain.exception;

/**
 * Исключение при ошибках валидации доменных объектов.
 */
public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }
}