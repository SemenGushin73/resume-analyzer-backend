package webapp.resumeanalyzer.exception;

/**
 * Исключение, возникающее при отсутствии запрашиваемого ресурса.
 *
 * Данное исключение выбрасывается, когда запрашиваемый ресурс
 * (например, объект в базе данных) не найден.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
