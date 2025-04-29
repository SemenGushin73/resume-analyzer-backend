package webapp.resumegenerator.infrastructure.controller;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.domain.service.PdfGenerationService;
import webapp.resumegenerator.domain.service.ResumeService;

/**
 * REST-контроллер для скачивания резюме в PDF формате.
 */
@RestController
@RequestMapping("/resumes")
public class ResumeFileDownloadController {

    private final PdfGenerationService pdfService;
    private final ResumeService resumeService;

    @Autowired
    public ResumeFileDownloadController(PdfGenerationService pdfService, ResumeService resumeService) {
        this.pdfService = pdfService;
        this.resumeService = resumeService;
    }

    /**
     * Метод для скачивания резюме в PDF формате.
     *
     * @param id Уникальный идентификатор резюме.
     * @param templateDTO шаблон по которому строится файл PDF.
     * @return resume.pdf
     */
    @PostMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResumePdf(@PathVariable UUID id, @RequestBody TemplateDTO template) {
        ResumeDto resume = resumeService.getResume( id );
        byte[] pdfBytes = pdfService.generateResumePdf( resume, template );
        return getResponcePdfFile( pdfBytes );
    }

    private ResponseEntity<Resource> getResponcePdfFile( byte[] pdfBytes ) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                   "attachment; filename=resume.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(new ByteArrayResource(pdfBytes));
    }
}
