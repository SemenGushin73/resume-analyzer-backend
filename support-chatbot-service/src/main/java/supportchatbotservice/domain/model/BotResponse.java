package supportchatbotservice.domain.model;

import java.util.Objects;

public final class BotResponse {
    private final String content;
    private final boolean isFallback;

    public BotResponse(String content, boolean isFallback) {
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.isFallback = isFallback;
    }

    public String getContent() {
        return content;
    }

    public boolean isFallback() {
        return isFallback;
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