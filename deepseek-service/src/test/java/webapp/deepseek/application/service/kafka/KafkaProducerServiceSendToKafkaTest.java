package webapp.deepseek.application.service.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kata.project.kafka.config.KafkaTopics;
import webapp.deepseek.application.service.KafkaProducerService;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceSendToKafkaTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private static final String KEY = "testKey";
    private static final String DATA = "testData";

    @Test
    void sendToKafka_ShouldSendData_WhenValidInput() {
        when(kafkaTemplate.send(eq(KafkaTopics.DEEPSEEK_GENERATION_RESULT), eq(KEY), eq(DATA)))
                .thenReturn(new CompletableFuture<>());

        kafkaProducerService.sendToKafka(KEY, DATA);

        verify(kafkaTemplate).send(KafkaTopics.DEEPSEEK_GENERATION_RESULT, KEY, DATA);
    }

    @Test
    void sendToKafka_ShouldHandleException_WhenKafkaFails() {
        when(kafkaTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("Kafka error"));

        assertDoesNotThrow(() -> kafkaProducerService.sendToKafka(KEY, DATA));
        verify(kafkaTemplate).send(KafkaTopics.DEEPSEEK_GENERATION_RESULT, KEY, DATA);
    }
}
