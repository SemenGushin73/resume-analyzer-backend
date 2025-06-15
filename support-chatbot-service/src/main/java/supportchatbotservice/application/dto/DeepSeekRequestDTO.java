package supportchatbotservice.application.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * DTO для запроса к внешнему AI-сервису DeepSeek.
 */
@Getter
public class DeepSeekRequestDTO {
    private String model = "deepseek/deepseek-chat";
    private List<Message> messages;

    public DeepSeekRequestDTO(String prompt) {
        this.messages = List.of(new Message("user", prompt));
    }

    @Getter
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeepSeekRequestDTO that = (DeepSeekRequestDTO) o;
        return Objects.equals(model, that.model) && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, messages);
    }
}