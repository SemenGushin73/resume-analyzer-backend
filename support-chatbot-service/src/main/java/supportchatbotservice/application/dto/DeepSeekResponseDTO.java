package supportchatbotservice.application.dto;


import lombok.Getter;

import java.util.Objects;

/**
 * DTO для ответа от внешнего AI-сервиса DeepSeek.
 */
@Getter
public class DeepSeekResponseDTO {
    private String answer;

    public DeepSeekResponseDTO() {
    }

    public DeepSeekResponseDTO(String answer) {
        this.answer = Objects.requireNonNull(answer, "Answer cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeepSeekResponseDTO that = (DeepSeekResponseDTO) o;
        return Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(answer);
    }
}