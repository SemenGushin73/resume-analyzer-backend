package webapp.resumegenerator.domain.PDFGenerator;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;

/*
 * Сервис для генерации HTML документа
 * для последующего превращения в PDF файл
*/
@Component
@Slf4j
@RequiredArgsConstructor
public class HTMLResumeGenerator {

    private final TemplateEngine templateEngine;
    private final TemplateToHtmlConvertor templateToHtmlConvertor;

    public String createHTML(ResumeDto resume, TemplateDTO template) {
        String htmlTemplate = templateToHtmlConvertor.convert( template );
        Context context = new Context();
        context.setVariable("resume", resume);
        System.out.println( htmlTemplate );
        System.out.println( templateEngine.process(htmlTemplate, context) );
        return templateEngine.process(htmlTemplate, context);
    }
}