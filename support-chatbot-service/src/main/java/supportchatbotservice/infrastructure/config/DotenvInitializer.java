package supportchatbotservice.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация для чтения данных из файла .env.
 */
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (!"support-chatbot-service".equals(appName)) {
            return;
        }

        final File localEnv = new File(System.getProperty("user.dir"), ".env");
        final File rootEnv = new File(System.getProperty("user.dir")).getParentFile();

        final String dotenvPath;
        if (localEnv.exists()) {
            dotenvPath = System.getProperty("user.dir");
        } else {
            dotenvPath = rootEnv.getAbsolutePath();
        }

        final Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env")
                .ignoreIfMissing()
                .load();

        final ConfigurableEnvironment env = applicationContext.getEnvironment();
        final Map<String, Object> propertyMap = new HashMap<>();
        dotenv.entries().forEach(entry -> propertyMap.put(entry.getKey(), entry.getValue()));
        env.getPropertySources().addFirst(new MapPropertySource("dotenv", propertyMap));
    }
}