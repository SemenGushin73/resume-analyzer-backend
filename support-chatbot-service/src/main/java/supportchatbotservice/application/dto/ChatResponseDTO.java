package supportchatbotservice.application.dto;


import lombok.Getter;

import java.util.Objects;

/**
 * DTO для передачи ответа чат-бота пользователю, включая флаг fallback-режима.
 */
@Getter
public class ChatResponseDTO {
    private String answer;
    private boolean isFallback;

    public ChatResponseDTO() {
    }

    public static ChatResponseDTO create(String answer, boolean isFallback) {
        Objects.requireNonNull(answer, "Answer cannot be null");
        return new ChatResponseDTO(answer, isFallback);
    }

    public ChatResponseDTO(String answer, boolean isFallback) {
        this.answer = Objects.requireNonNull(answer, "Answer cannot be null");
        this.isFallback = isFallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChatResponseDTO that = (ChatResponseDTO) o;
        return isFallback == that.isFallback && Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answer, isFallback);
    }
}