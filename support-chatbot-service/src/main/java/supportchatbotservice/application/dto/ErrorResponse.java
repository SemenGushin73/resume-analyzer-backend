package supportchatbotservice.application.dto;


import java.util.Objects;

public final class ErrorResponse {
    private final String error;

    public ErrorResponse(String error) {
        this.error = Objects.requireNonNull(error, "Error cannot be null");
    }

    public String getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ErrorResponse that = (ErrorResponse) o;
        return Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(error);
    }
}