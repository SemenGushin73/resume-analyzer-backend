package supportchatbotservice.application.dto;


import lombok.Getter;

import java.util.Objects;

/**
 * DTO для запроса к внешнему AI-сервису DeepSeek.
 */
@Getter
public class DeepSeekRequestDTO {
    private String message;

    public DeepSeekRequestDTO() {
    }

    public DeepSeekRequestDTO(String message) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeepSeekRequestDTO that = (DeepSeekRequestDTO) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }
}