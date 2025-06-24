package ru.kata.project.chatbotDTO;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.Objects;

/**
 * DTO для передачи сообщения пользователя и идентификатора сессии в чат-сервис.
 */
@Getter
public class ChatRequestDTO {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotBlank(message = "SessionId cannot be blank")
    private String sessionId;

    public ChatRequestDTO() {
    }

    public static ChatRequestDTO create(String message, String sessionId) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("SessionId cannot be empty");
        }
        return new ChatRequestDTO(message, sessionId);
    }

    private ChatRequestDTO(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChatRequestDTO that = (ChatRequestDTO) o;
        return Objects.equals(message, that.message) && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, sessionId);
    }

}