package supportchatbotservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * DTO для ответа от внешнего AI-сервиса DeepSeek через OpenRouter.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSeekResponseDTO {
    private List<Choice> choices;

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public String getAnswer() {
        return (choices != null && !choices.isEmpty() && choices.get(0).message != null)
                ? choices.get(0).message.content
                : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public String role;
        public String content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeepSeekResponseDTO that = (DeepSeekResponseDTO) o;
        return Objects.equals(choices, that.choices);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(choices);
    }
}