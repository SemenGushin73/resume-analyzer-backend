package webapp.resumeanalyzer.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import webapp.resumeanalyzer.domain.model.ResumeFile;
import webapp.resumeanalyzer.domain.service.ResumeFileService;

import java.io.IOException;
import java.util.List;

/**
 * Тестовый класс для проверки функциональности ResumeFile.
 */
@Tag(name = "ResumeFile API", description = "Управление файлами резюме")
@RestController
@RequestMapping("/resumes/files")
public class ResumeFileTestController {

    private final ResumeFileService resumeFileService;

    @Autowired
    public ResumeFileTestController(ResumeFileService resumeFileService) {
        this.resumeFileService = resumeFileService;
    }

    @Operation(summary = "Загрузка резюме в БД",
            description = "Загружает файл резюме в формате .pdf или .docx в БД",
            parameters = {
                    @Parameter(name = "ownerId", in = ParameterIn.QUERY,
                            description = "Идентификатор владельца резюме файла",
                            required = true, example = "550e8400-e29b-41d4-a716-446655440000"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Резюме успешно загружено",
                            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(type = "string", example = "Резюме успешно загружено!"))),
                    @ApiResponse(responseCode = "400", description = "Недопустимый формат файла",
                            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(type = "string", example = "Недопустимый формат файла!")))
            })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadResumeFile(@RequestPart("file") MultipartFile file,
                                                   @RequestParam String ownerId)
            throws IOException {

        if (file.getContentType().equals("application/pdf") ||
            file.getContentType()
                    .equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {

            resumeFileService.uploadResumeFile(
                    resumeFileService.createResumeFile(file, ownerId));
            return ResponseEntity.ok("Файл успешно загружен");
        } else {
            return ResponseEntity.badRequest()
                    .body("Недопустимый тип файла! Принимаются только файлы .docx и .pdf!");
        }
    }

    @Operation(summary = "Получить инфо о всех резюме файлах",
            description = "Возвращает информацию о всех файлах резюме хранящихся в БД")
    @ApiResponse(responseCode = "200", description = "Файлы найдены")
    @ApiResponse(responseCode = "204", description = "Еще нет загруженных резюме")
    @GetMapping("/all")
    public ResponseEntity<List<ResumeFile>> getResumeFile() {
        if (resumeFileService.getAllResumeFiles().isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(resumeFileService.getAllResumeFiles());
        }
    }

    @Operation(summary = "Удалить файл резюме по ID",
            description = "Удаление файла резюме из БД по ID",
            parameters = {
                    @Parameter(name = "id", description = "ID файла резюме для удаления",
                            required = true, example = "f9876543-210e-dcba-9876-543210fedcba")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл удален",
                            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(type = "string", example = "Файл удален!"))),
                    @ApiResponse(responseCode = "400", description = "Файл не найден")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteResumeFile(@PathVariable String id) {
        if (resumeFileService.getResumeFileById(id) == null) {
            return ResponseEntity.badRequest().build();
        } else {
            resumeFileService.deleteResumeFile(resumeFileService.getResumeFileById(id));
            return ResponseEntity.ok("Файл удален!");
        }
    }

    @Operation(summary = "Загрузить файл резюме",
            description = "Загружает файл резюме из БД по идентификатору",
            parameters = {
                    @Parameter(name = "id", description = "ID файла резюме для загрузки",
                            required = true, example = "f9876543-210e-dcba-9876-543210fedcba")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Загрузка файла",
                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Файл не найден")
            }
    )
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResumeFile(@PathVariable String id) throws IOException {
        if (resumeFileService.getResumeFileById(id) == null) {
            return ResponseEntity.badRequest().build();
        } else {

            final ResumeFile resumeFile = resumeFileService.getResumeFileById(id);
            final Resource fileResource = new FileSystemResource(resumeFileService.getFile(id));

            return ResponseEntity.ok()
                    .headers(resumeFileService.getHeaders(resumeFile))
                    .contentLength(resumeFileService.getFile(id).length())
                    .contentType(MediaType.parseMediaType(resumeFile.getFileType()))
                    .body(fileResource);
        }
    }
}
