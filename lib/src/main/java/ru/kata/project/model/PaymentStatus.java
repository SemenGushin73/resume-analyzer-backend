package ru.kata.project.model;

public enum PaymentStatus {
    SUCCEEDED,
    FAILED,
    UNKNOWN;

    public static PaymentStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return UNKNOWN;
        }
        try {
            return PaymentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}