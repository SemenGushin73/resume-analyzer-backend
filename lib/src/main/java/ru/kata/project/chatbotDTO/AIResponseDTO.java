package ru.kata.project.chatbotDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DTO для ответа от внешнего AI-сервиса DeepSeek через OpenRouter.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIResponseDTO {
    private List<Choice> choices;

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Optional<String> getAnswer() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message != null) {
            return Optional.ofNullable(choices.get(0).message.content);
        }
        return Optional.empty();
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
        AIResponseDTO that = (AIResponseDTO) o;
        return Objects.equals(choices, that.choices);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(choices);
    }
}