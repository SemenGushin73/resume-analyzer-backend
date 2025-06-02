package supportchatbotservice.domain.model;

public class BotResponse {
    private String content;
    private boolean fallback;

    public BotResponse(String content, boolean fallback) {
        this.content = content;
        this.fallback = fallback;
    }

    public String getContent() {
        return content;
    }

    public boolean isFallback() {
        return fallback;
    }
}