package ru.javabegin.micro.planner.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.MessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.kata.project.kafka.AnalysisRequest;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public ConsumerFactory<String, AnalysisRequest> consumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  // укажите свой сервер
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "ru.kata.project.kafka");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AnalysisRequest.class);

        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JsonMessageConverter();
    }
}
