package webapp.resumegenerator.infrastructure.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.domain.service.PdfGenerationService;

@RestController
@RequestMapping("/resumes")
public class ResumeFileDownloadController {

    private final PdfGenerationService pdfService;

    
    @Autowired
    public ResumeFileDownloadController(PdfGenerationService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/download")
    public ResponseEntity<Resource> downloadResumePdf(@RequestBody ResumeRequest request) {
        
        byte[] pdfBytes = pdfService.generateResumePdf(request.getResumeDto(), request.getTemplateDTO());
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                   "attachment; filename=resume.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(new ByteArrayResource(pdfBytes));
    }

    public static class ResumeRequest {
        private ResumeDto resumeDto;
        private TemplateDTO templateDTO;

        // Геттеры и сеттеры
        public ResumeDto getResumeDto() {
            return resumeDto;
        }

        public void setResumeDto(ResumeDto resumeDto) {
            this.resumeDto = resumeDto;
        }

        public TemplateDTO getTemplateDTO() {
            return templateDTO;
        }

        public void setTemplateDTO(TemplateDTO templateDTO) {
            this.templateDTO = templateDTO;
        }
    }
}
