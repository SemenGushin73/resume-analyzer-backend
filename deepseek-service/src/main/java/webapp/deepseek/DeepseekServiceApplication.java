package webapp.deepseek;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@OpenAPIDefinition(info =
@Info(title = "Deepseek Service", version = "1.0.0", description = "Documentation Deepseek Service API v1.0.0")
)
public class DeepseekServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeepseekServiceApplication.class, args);
    }
}
