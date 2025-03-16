package webapp.deepseek.exception;

/**
 * Исключение, возникающее при передаче некорректных данных в запросе.
 *
 * Данное исключение выбрасывается, когда входные параметры запроса
 * не соответствуют ожидаемому формату или содержат недопустимые значения.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
