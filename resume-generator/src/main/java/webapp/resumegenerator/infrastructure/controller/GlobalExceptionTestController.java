package webapp.resumegenerator.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webapp.resumegenerator.exception.ExternalServiceException;
import webapp.resumegenerator.exception.InvalidRequestException;
import webapp.resumegenerator.exception.KafkaProcessingException;
import webapp.resumegenerator.exception.ResourceNotFoundException;

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