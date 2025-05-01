package webapp.resumegenerator.infrastructure.controller;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.domain.service.PdfGenerationService;
import webapp.resumegenerator.domain.service.ResumeService;

/**
 * REST-контроллер для скачивания резюме в PDF формате.
 */
@RestController
@RequestMapping("/resumes")
@Tag(name = "Resume Download", description = "API для генерации и скачивания резюме в PDF формате")
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
    @Operation(
        summary = "Скачать резюме в PDF",
        description = "Генерирует и возвращает резюме в формате PDF на основе указанного шаблона"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "PDF файл резюме",
            content = @Content(mediaType = "application/pdf",
                              schema = @Schema(type = "string", format = "binary"))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Резюме не найдено"
            ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверный запрос"
        )
    })
    public ResponseEntity<Resource> downloadResumePdf(
        @Parameter(
            description = "UUID резюме",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        @PathVariable UUID id,
        
        @Parameter(
            description = "Шаблон для генерации PDF",
            required = true,
            content = @Content(schema = @Schema(implementation = TemplateDTO.class))
        )
        @RequestBody TemplateDTO template) {
        ResumeDto resume = resumeService.getResume(id);
        byte[] pdfBytes = pdfService.generateResumePdf(resume, template);
        return getResponsePdfFile(id, pdfBytes);
    }

    private ResponseEntity<Resource> getResponsePdfFile(UUID id, byte[] pdfBytes) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                   "attachment; filename=resume_" + id.toString() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(new ByteArrayResource(pdfBytes));
    }
}
