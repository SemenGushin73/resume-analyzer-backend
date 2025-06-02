package PDFGeneratorTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.domain.PDFGenerator.HTMLResumeGenerator;
import webapp.resumegenerator.domain.PDFGenerator.TemplateToHtmlConvertor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HTMLResumeGeneratorTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private TemplateToHtmlConvertor templateToHtmlConvertor;

    @InjectMocks
    private HTMLResumeGenerator htmlResumeGenerator;

    @Test
    void createHTML_ShouldProcessTemplateCorrectly() {
        ResumeDto originalResume = new ResumeDto();
        originalResume.setId(UUID.randomUUID());
        ResumeDto resume = new ResumeDto(originalResume);
        TemplateDTO template = new TemplateDTO();
        String expectedHtml = "<html>Generated HTML</html>";
        String convertedTemplate = "template-name";

        when(templateToHtmlConvertor.convert(template)).thenReturn(convertedTemplate);
        when(templateEngine.process(eq(convertedTemplate), any(Context.class)))
                .thenReturn(expectedHtml);

        String result = htmlResumeGenerator.createHTML(resume, template);

        assertEquals(expectedHtml, result);
        verify(templateToHtmlConvertor).convert(template);
        verify(templateEngine).process(eq(convertedTemplate), any(Context.class));
    }
}