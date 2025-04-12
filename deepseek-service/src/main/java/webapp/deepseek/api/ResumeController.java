package webapp.deepseek.api;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import webapp.deepseek.domain.service.DeepSeekService;

@RestController
@RequestMapping("/api/resume")
@Tag(name = "Resume Analysis", description = "API для анализа и оценки резюме")
public class ResumeController {

    private final DeepSeekService deepSeekService;

    public ResumeController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Анализ резюме", description = "Анализирует резюме на соответствие описанию вакансии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный анализ резюме"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<String> analyzeResume(
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobDescription") String jobDescription) {

        if (!deepSeekService.isValidResumeOrJobDescription(resumeText) ||
                !deepSeekService.isValidResumeOrJobDescription(jobDescription)) {
            return ResponseEntity.badRequest().body("Некорректный текст резюме или описания вакансии.");
        }

        try {
            return ResponseEntity.ok(deepSeekService.analyzeResume(resumeText, jobDescription));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка обработки: " + e.getMessage());
        }
    }
}