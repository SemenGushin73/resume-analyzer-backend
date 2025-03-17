package webapp.resumeanalyzer.infrastructure.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webapp.resumeanalyzer.exception.ExternalServiceException;
import webapp.resumeanalyzer.exception.InvalidRequestException;
import webapp.resumeanalyzer.exception.KafkaProcessingException;
import webapp.resumeanalyzer.exception.ResourceNotFoundException;

/**
 * Контроллер для тестирования обработки глобальных исключений.
 *
 * Данный контроллер предоставляет эндпоинты, вызывающие различные исключения,
 * чтобы проверить работу GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/test")
public class GlobalExceptionTestController {

    @GetMapping("/resource-not-found")
    public String throwResourceNotFoundException() {
        throw new ResourceNotFoundException("Resource not found");
    }

    @GetMapping("/invalid-request")
    public String throwInvalidRequestException() {
        throw new InvalidRequestException("Invalid request data");
    }

    @GetMapping("/kafka-error")
    public String throwKafkaProcessingException() {
        throw new KafkaProcessingException("Kafka error");
    }

    @GetMapping("/external-service-error")
    public String throwExternalServiceException() {
        throw new ExternalServiceException("External service error!!!");
    }

    @GetMapping("/global-error")
    public String throwGlobalException() {
        throw new RuntimeException("Unexpected error");
    }
}