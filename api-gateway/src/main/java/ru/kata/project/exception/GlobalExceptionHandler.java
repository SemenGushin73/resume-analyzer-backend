package ru.kata.project.exception;

import feign.FeignException;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Ошибка при вызове удаленного сервиса");
        errorResponse.put("status", ex.status());
        errorResponse.put("error", ex.contentUTF8());

        return ResponseEntity.status(ex.status()).body(errorResponse);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Ошибка при вызове REST-клиента");
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleTimeoutException(TimeoutException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Превышено время ожидания ответа от сервиса");
        errorResponse.put("status", HttpStatus.GATEWAY_TIMEOUT.value());

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Внутренняя ошибка API Gateway");
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

