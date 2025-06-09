package supportchatbotservice.infrastructure.client;

import feign.RequestInterceptor;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для DeepSeek API.
 */
@Configuration
public class DeepSeekConfig {

    private final Dotenv dotenv;

    public DeepSeekConfig(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        final String apiKey = dotenv.get("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("DEEPSEEK_API_KEY is not set in .env file");
        }

        return request -> {
            request.header("Authorization", "Bearer " + apiKey);
            request.header("HTTP-Referer", "YOUR_SITE_URL");
            request.header("X-Title", "Your App Name");
        };
    }
}