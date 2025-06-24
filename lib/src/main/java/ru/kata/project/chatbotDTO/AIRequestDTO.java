package ru.kata.project.chatbotDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * Универсальный DTO для запроса к AI.
 */
@Slf4j
@Getter
public class AIRequestDTO {
    private final String sessionId;
    private final String model;
    private final List<Message> messages;

    public AIRequestDTO(String message, String sessionId) {
        this.sessionId = sessionId;
        this.model = System.getenv().getOrDefault("DEEPSEEK_MODEL", "mistralai/mistral-7b-instruct");
        this.messages = List.of(new Message("user", message));
        log.info("Модель AI: {}", this.model);
    }

    public String getMessage() {
        return messages.get(0).getContent();
    }

    public static AIRequestDTO create(String message, String sessionId) {
        return new AIRequestDTO(message, sessionId);
    }

    @Getter
    @AllArgsConstructor
    public static class Message {
        private final String role;
        private final String content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AIRequestDTO that = (AIRequestDTO) o;
        return Objects.equals(model, that.model) &&
                Objects.equals(messages, that.messages) &&
                Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, messages, sessionId);
    }
}