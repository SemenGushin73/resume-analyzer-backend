package ru.kata.project.config.OpenAPI;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@OpenAPIDefinition
@Configuration
public class OpenApiConfig {
    @Bean
    public List<GroupedOpenApi> apis(GatewayProperties gatewayProperties) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        gatewayProperties.getRoutes().forEach(route -> {
            String name = route.getId();
            GroupedOpenApi api = GroupedOpenApi.builder()
                    .group(name)
                    .pathsToMatch("/" + name + "/**")
                    .build();
            groups.add(api);
        });

        return groups;
    }

//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .addServersItem(new Server()
//                        .url("http://localhost:8765/openapi")
//                        .description("API Gateway"));
//    }
}
