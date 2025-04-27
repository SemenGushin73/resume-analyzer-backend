package webapp.resumeanalyzer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@OpenAPIDefinition(info =
@Info(title = "Resume Analyzer", version = "1.0.0", description = "Documentation Resume Analyzer API v1.0.0")
)
public class ResumeAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeAnalyzerApplication.class, args);
    }
}
