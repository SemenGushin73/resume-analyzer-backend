package supportchatbotservice.application.dto;


import java.util.Objects;

public final class DeepSeekResponse {
    private final String answer;

    public DeepSeekResponse(String answer) {
        this.answer = Objects.requireNonNull(answer, "Answer cannot be null");
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeepSeekResponse that = (DeepSeekResponse) o;
        return Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(answer);
    }
}