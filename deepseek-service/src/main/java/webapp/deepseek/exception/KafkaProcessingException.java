package webapp.deepseek.exception;

/**
 * Исключение, возникающее при ошибках обработки сообщений в Kafka.
 *
 * Данное исключение выбрасывается в случаях, когда возникает проблема
 * при отправке или получении сообщений через Kafka.
 */
public class KafkaProcessingException extends RuntimeException {
    public KafkaProcessingException(String message) {
        super(message);
    }
}
