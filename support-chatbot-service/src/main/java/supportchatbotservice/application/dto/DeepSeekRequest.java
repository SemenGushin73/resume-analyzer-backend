package supportchatbotservice.application.dto;


import java.util.Objects;

public final class DeepSeekRequest {
    private final String message;

    public DeepSeekRequest(String message) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeepSeekRequest that = (DeepSeekRequest) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }
}