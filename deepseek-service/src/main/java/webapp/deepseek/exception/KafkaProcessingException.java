package webapp.deepseek.exception;

public class KafkaProcessingException extends RuntimeException {
    public KafkaProcessingException(String message) {
        super(message);
    }
}
