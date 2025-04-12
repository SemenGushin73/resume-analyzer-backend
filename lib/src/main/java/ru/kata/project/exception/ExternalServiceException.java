package ru.kata.project.exception;

/**
 * Исключение, возникающее при ошибках во внешних сервисах.
 *
 * Данное исключение используется, когда внешний сервис возвращает ошибку
 * или недоступен, что препятствует корректному выполнению запроса.
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}