package webapp.resumegenerator.domain.PDFGenerator;

import org.springframework.stereotype.Component;

import ru.kata.project.resumeDto.TemplateDTO;

/*
 * 
 * конвертер контекста template в html
 * TODO: дописать метод
*/
@Component
public class TemplateToHtmlConvertor {
    public String convert( TemplateDTO template) {
        return template.getContent();
    }
}
