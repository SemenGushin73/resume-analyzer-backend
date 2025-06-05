package supportchatbotservice.application.dto;


import java.util.Objects;

public final class ChatResponse {
    private final String answer;
    private final boolean isFallback;

    public static ChatResponse create(String answer, boolean isFallback) {
        Objects.requireNonNull(answer, "Answer cannot be null");
        return new ChatResponse(answer, isFallback);
    }

    public ChatResponse(String answer, boolean isFallback) {
        this.answer = Objects.requireNonNull(answer, "Answer cannot be null");
        this.isFallback = isFallback;
    }

    public String getAnswer() {
        return answer;
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
        final ChatResponse that = (ChatResponse) o;
        return isFallback == that.isFallback && Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answer, isFallback);
    }
}