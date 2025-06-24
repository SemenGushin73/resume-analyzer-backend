package ru.kata.project.chatbotDTO;


import lombok.Getter;

import java.util.Objects;

/**
 * DTO для передачи информации об ошибке клиенту.
 */
@Getter
public class ErrorResponseDTO {
    private String error;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(String error) {
        this.error = Objects.requireNonNull(error, "Error cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ErrorResponseDTO that = (ErrorResponseDTO) o;
        return Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(error);
    }
}