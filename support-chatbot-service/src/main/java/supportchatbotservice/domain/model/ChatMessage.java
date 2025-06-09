package supportchatbotservice.domain.model;

import lombok.Getter;

import java.util.Objects;

/**
 * Сообщение чата с содержимым и идентификатором сессии.
 */
@Getter
public class ChatMessage {
    private String content;
    private String sessionId;

    public ChatMessage() {
    }

    public ChatMessage(String content, String sessionId) {
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.sessionId = Objects.requireNonNull(sessionId, "SessionId cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChatMessage that = (ChatMessage) o;
        return Objects.equals(content, that.content) && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, sessionId);
    }

    public void validate() {
        if (content.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
    }
}