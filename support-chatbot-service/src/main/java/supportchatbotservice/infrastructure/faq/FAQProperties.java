package supportchatbotservice.infrastructure.faq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "faq")
@Component
public class FAQProperties {
    private Map<String, String> answers;
}