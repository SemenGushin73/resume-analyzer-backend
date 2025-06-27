package supportchatbotservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import lombok.extern.slf4j.Slf4j;
import supportchatbotservice.infrastructure.config.DotenvInitializer;
import supportchatbotservice.infrastructure.faq.FAQProperties;

/**
 * Главный класс микросервиса чат-бота поддержки.
 */
@EnableCaching
@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(FAQProperties.class)
@Slf4j
public class SupportChatbotServiceApplication {

    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(SupportChatbotServiceApplication.class);
        app.addInitializers(new DotenvInitializer());
        app.run(args);
    }

    @Bean
    public CommandLineRunner checkFaqConfig(FAQProperties faqProperties) {
        return args -> log.info("Loaded FAQ questions: {}", faqProperties.getAnswers().keySet());
    }
}