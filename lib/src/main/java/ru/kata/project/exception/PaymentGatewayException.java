package ru.kata.project.exception;

/**
 * Пользовательское исключение, связанное с проблемами при взаимодействии с внешним платежным шлюзом
 **/
public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}