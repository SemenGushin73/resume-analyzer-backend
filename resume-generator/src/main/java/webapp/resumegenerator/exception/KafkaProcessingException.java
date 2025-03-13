package webapp.resumegenerator.exception;

public class KafkaProcessingException extends RuntimeException {
    public KafkaProcessingException(String message) {
        super(message);
    }
}
