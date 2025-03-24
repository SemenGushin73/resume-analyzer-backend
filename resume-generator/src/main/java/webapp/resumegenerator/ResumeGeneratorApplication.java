package webapp.resumegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "webapp.resumegenerator",
        "webapp.common"
})
public class ResumeGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeGeneratorApplication.class, args);
    }
}
