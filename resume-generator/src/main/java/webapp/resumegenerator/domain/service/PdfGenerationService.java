package webapp.resumegenerator.domain.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.BaseFont;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.exception.PdfGenerationException;

/*
 * TODO: ПОДДЕРЖКА КИРРИЛИЦЫ
 * 
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private final TemplateEngine templateEngine;

    public byte[] generateResumePdf(ResumeDto resume, TemplateDTO template) {
        try {
            String filledHtml = fillTemplateHtml(template.getContent(), resume);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            
            renderer.setDocumentFromString(filledHtml);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
            
            System.out.println("aaaaaaaaaaaaaaaaaa");

            byte[] pdfBytes = outputStream.toByteArray();
            
            validatePdfSize(pdfBytes);
            
            return pdfBytes;

        } catch (Exception e) {
            log.error("PDF generation failed for resume {}", resume.getId(), e);
            throw new PdfGenerationException("Failed to generate PDF", e);
        }
    }

    private String fillTemplateHtml(String htmlTemplate, ResumeDto resume) {
        Context context = new Context();
        context.setVariable("resume", resume);
        return templateEngine.process(htmlTemplate, context);
    }

    private void validatePdfSize(byte[] pdfBytes) {
        if (pdfBytes.length > MAX_FILE_SIZE) {
            throw new PdfGenerationException(
                String.format("Generated PDF size %dB exceeds maximum of 2MB", pdfBytes.length)
            );
        }
    }
}