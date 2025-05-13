package ru.kata.project.config.kafkaConfig;

import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.MessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.AnalysisResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Этот класс является конфигурационным классом, который настраивает
 * подключение к Apache Kafka и определяет, как микросервис
 * будет получать (consume) сообщения из Kafka-топиков. Настраивает Consumer (потребителя)
 * для чтения сообщений из Kafka. Определяет десериализацию JSON в объекты Java (AnalysisResult).
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public ConsumerFactory<String, AnalysisRequest> consumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
//        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "ru.kata.project.kafka");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AnalysisResult.class);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JsonMessageConverter();
    }
}
