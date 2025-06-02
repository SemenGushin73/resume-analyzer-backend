package supportchatbotservice.domain.model;

public class ChatMessage {
    private String content;
    private String sessionId;

    public ChatMessage(String content, String sessionId) {
        this.content = content;
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void validate() {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
    }
}