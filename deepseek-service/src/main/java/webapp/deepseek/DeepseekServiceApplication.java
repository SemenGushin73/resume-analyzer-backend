package webapp.deepseek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {
		"webapp.deepseek",
		"webapp.common"
})
@EnableFeignClients
public class DeepseekServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeepseekServiceApplication.class, args);
	}
}
