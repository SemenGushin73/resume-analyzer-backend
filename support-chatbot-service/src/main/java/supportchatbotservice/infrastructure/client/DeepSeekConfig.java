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

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API KEY not provided");
        }

        log.info("Loaded DEEPSEEK_API_KEY: {}...", apiKey.substring(0, 8));
        return request -> {
            request.header("Authorization", "Bearer " + apiKey);
            request.header("Content-Type", "application/json");
        };
    }
}