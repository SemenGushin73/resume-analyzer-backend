package webapp.resumegenerator.PDFstuff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeleafConfig {
    
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }
    
    private ITemplateResolver templateResolver() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        return templateResolver;
    }
}