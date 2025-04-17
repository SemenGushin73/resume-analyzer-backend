package webapp.deepseekservice.kafka;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.config.KafkaTopics;
import webapp.deepseek.DeepseekServiceApplication;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки работы приёма Kafka-сообщений модулем deepseek-service
 */
@SpringBootTest(classes = DeepseekServiceApplication.class)
@EmbeddedKafka(topics = KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS,
        brokerProperties = {
            "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        })
@DirtiesContext
public class KafkaListenerIntegrationTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(KafkaListenerIntegrationTest.class);

    @Autowired
    @Qualifier("embeddedKafka")
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private ListAppender<ILoggingEvent> memoryAppender;

    @BeforeEach
    public void setup() {
        Logger logger = (Logger) LoggerFactory
                .getLogger("deepseek-service/src/main/java/webapp/deepseek/" +
                        "application/service/KafkaConsumerService.java");
        memoryAppender = (ListAppender<ILoggingEvent>) logger.getAppender("MEMORY");

        if (memoryAppender != null) {
            memoryAppender.list.clear();
        } else {
            throw new IllegalStateException("MEMORY Appender не найден в конфигурации Logback.");
        }
    }

    @Test
    void shouldProcessAnalysisRequestAndLogCorrectly() throws Exception {
        String requestId = UUID.randomUUID().toString();
        String resumeText = "Java developer with Spring experience.";
        String jobDescription = "Looking for a backend developer with Java 21, Kafka and PostgreSQL knowledge.";
        AnalysisRequest analysisRequest = new AnalysisRequest(resumeText, jobDescription);
        String messagePayload = objectMapper.writeValueAsString(analysisRequest);

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, requestId, messagePayload);
        producer.send(record);
        producer.flush();
        producer.close();
        log.info("Тестовое сообщение отправлено в топик {}: key={}, payload={}",
                KafkaTopics.DEEPSEEK_ANALYSIS_REQUESTS, requestId, messagePayload);

        MessageListenerContainer listenerContainer = kafkaListenerEndpointRegistry
                .getListenerContainers()
                .stream()
                .filter(container -> container.getGroupId() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kafka listener container не найден"));

        Thread.sleep(5000);

        List<String> loggedMessages = memoryAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertThat(loggedMessages).anyMatch(msg -> msg.contains("Получено сообщение: requestId="
                + requestId));
        assertThat(loggedMessages).anyMatch(msg -> msg.contains("данные=" + analysisRequest));
        assertThat(loggedMessages).noneMatch(msg -> msg.contains("Ошибка обработки запроса"));
    }
}