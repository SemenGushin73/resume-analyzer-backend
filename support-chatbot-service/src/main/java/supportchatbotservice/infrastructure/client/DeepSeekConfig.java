package supportchatbotservice.infrastructure.client;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для DeepSeek API.
 */
@Configuration
@Slf4j
public class DeepSeekConfig {

    @Bean
    public RequestInterceptor apiKeyInterceptor(@Value("${deepseek.api.key}") String apiKey) {
        log.info("API Key loaded successfully.");
        return request -> {
            request.header("Authorization", "Bearer " + apiKey);
            request.header("HTTP-Referer", "http://localhost:8085");
            request.header("X-Title", "Support Chatbot");
        };
    }
}