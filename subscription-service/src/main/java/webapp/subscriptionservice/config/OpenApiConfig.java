package webapp.subscriptionservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Класс для настройки документации API с использованием OpenAPI (Swagger UI).
 * Использует аннотацию @OpenAPIDefinition для базовой конфигурации
 * и предоставляет OpenAPI Bean для определения пользовательских элементов,
 * таких как серверы, информация о приложении, теги и т.д.
 **/
@OpenAPIDefinition
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("http://localhost:8765/subscription")
                        .description("Subscription API"));

    }
}
