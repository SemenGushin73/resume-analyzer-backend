package supportchatbotservice.domain.model;

import lombok.Getter;

import java.util.Objects;

/**
 * Ответ чат-бота, содержащий текст ответа и флаг fallback-режима.
 */
@Getter
public class BotResponse {
    private String content;
    private boolean isFallback;

    public BotResponse() {
    }

    public BotResponse(String content, boolean isFallback) {
        this.content = Objects.requireNonNull(content, "Content cannot be null");
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
        final BotResponse that = (BotResponse) o;
        return isFallback == that.isFallback && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, isFallback);
    }
}