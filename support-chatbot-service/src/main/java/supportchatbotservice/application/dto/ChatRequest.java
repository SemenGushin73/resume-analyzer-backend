package supportchatbotservice.application.dto;


import java.util.Objects;

public final class ChatRequest {
    private final String message;
    private final String sessionId;

    public static ChatRequest create(String message, String sessionId) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("SessionId cannot be empty");
        }
        return new ChatRequest(message, sessionId);
    }

    private ChatRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChatRequest that = (ChatRequest) o;
        return Objects.equals(message, that.message) && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, sessionId);
    }

}