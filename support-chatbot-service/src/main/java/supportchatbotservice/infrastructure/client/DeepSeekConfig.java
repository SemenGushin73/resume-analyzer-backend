package supportchatbotservice.infrastructure.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeepSeekConfig {
    @Value("${deepseek.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return request -> {
            request.header("Authorization", "Bearer " + apiKey);
            request.header("HTTP-Referer", "YOUR_SITE_URL");
            request.header("X-Title", "Your App Name");
        };
    }
}