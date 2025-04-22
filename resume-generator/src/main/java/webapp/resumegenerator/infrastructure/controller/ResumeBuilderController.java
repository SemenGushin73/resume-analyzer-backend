package webapp.resumegenerator.infrastructure.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webapp.resumegenerator.domain.service.ResumeBuilder;

/**
 * Контроллер для обработки запросов на построение резюме.
 * Этот класс принимает JSON с данными резюме, передаёт их в сервис ResumeBuilder
 * для обработки и возвращает сгенерированное резюме в формате JSON.
 */
@RestController
@RequestMapping("/builder")
public class ResumeBuilderController {

    private final ResumeBuilder resumeBuilder;

    @Autowired
    public ResumeBuilderController(ResumeBuilder resumeBuilder) {
        this.resumeBuilder = resumeBuilder;
    }

    /**
     * Обрабатывает запрос на построение резюме.
     *
     * @param resumeJson JSON-объект с данными резюме.
     * @return Ответ со сгенерированным резюме в формате JSON и статусом 201 Created.
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<JsonNode> buildResume(@Valid @RequestBody JsonNode resumeJson) {
        JsonNode generatedResume = resumeBuilder.processResume(resumeJson);
        return ResponseEntity.status(HttpStatus.CREATED).body(generatedResume);
    }
}
