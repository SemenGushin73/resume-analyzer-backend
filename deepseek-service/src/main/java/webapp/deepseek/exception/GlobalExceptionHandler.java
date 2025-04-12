package webapp.deepseek.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kata.project.exception.ExternalServiceException;
import ru.kata.project.exception.InvalidRequestException;
import ru.kata.project.exception.KafkaProcessingException;
import ru.kata.project.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всех контроллеров в приложении.
 * <p>
 * Этот класс перехватывает различные исключения, возникающие в процессе работы приложения,
 * и возвращает соответствующие HTTP-ответы с описанием ошибки и кодом статуса.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение, возникающее при отсутствии запрашиваемого ресурса.
     *
     * @param ex Исключение {@link ResourceNotFoundException}
     * @return Ответ с HTTP-статусом 404 (Not Found) и сообщением об ошибке
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает исключение, возникающее при некорректном запросе пользователя.
     *
     * @param ex Исключение {@link InvalidRequestException}
     * @return Ответ с HTTP-статусом 400 (Bad Request) и сообщением об ошибке
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> handleInvalidRequestException(InvalidRequestException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения, возникающие при ошибках обработки сообщений в Kafka.
     *
     * @param ex Исключение {@link KafkaProcessingException}
     * @return Ответ с HTTP-статусом 500 (Internal Server Error) и сообщением об ошибке
     */
    @ExceptionHandler(KafkaProcessingException.class)
    public ResponseEntity<Object> handleKafkaProcessingException(KafkaProcessingException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Обрабатывает исключения, возникающие при ошибках во внешних сервисах.
     *
     * @param ex Исключение {@link ExternalServiceException}
     * @return Ответ с HTTP-статусом 500 (Internal Server Error) и сообщением об ошибке
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceException(ExternalServiceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Обрабатывает все остальные исключения, которые не были обработаны выше.
     *
     * @param ex Любое необработанное исключение
     * @return Ответ с HTTP-статусом 500 (Internal Server Error) и сообщением "Internal Server Error"
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Internal Server Error");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
