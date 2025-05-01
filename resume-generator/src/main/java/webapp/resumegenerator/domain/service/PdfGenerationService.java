package webapp.resumegenerator.domain.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.exception.PdfGenerationException;

/*
 * абстрактный сервис по генерации PDF файлов резюме
 * 
*/
@Service
@Slf4j
public abstract class PdfGenerationService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public byte[] generateResumePdf(ResumeDto resume, TemplateDTO template) {
        try {
            byte[] pdfFile = createPdfFile( resume, template );
            validatePdfSize(pdfFile);
            return pdfFile;
        } catch (Exception e) {
            log.error("PDF generation failed for resume {}", resume.getId(), e);
            throw new PdfGenerationException("Failed to generate PDF", e);
        }
    }

    public abstract byte[] createPdfFile(ResumeDto resume, TemplateDTO template) throws Exception;

    private void validatePdfSize(byte[] pdfBytes) {
        if (pdfBytes.length > MAX_FILE_SIZE) {
            String errorMessage = String.format("Generated PDF size %dB exceeds maximum of 2MB", pdfBytes.length);
            throw new PdfGenerationException( errorMessage );
        }
    }
}