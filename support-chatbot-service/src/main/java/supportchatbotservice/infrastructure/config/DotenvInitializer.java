package supportchatbotservice.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация для чтения данных из файла .env.
 */
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        Map<String, Object> propertyMap = new HashMap<>();

        dotenv.entries().forEach(entry -> propertyMap.put(entry.getKey(), entry.getValue()));

        env.getPropertySources().addFirst(new MapPropertySource("dotenv", propertyMap));
    }
}