package webapp.resumegenerator.infrastructure.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
    private final ObjectMapper objectMapper;

    @Autowired
    public ResumeBuilderController(ResumeBuilder resumeBuilder, ObjectMapper objectMapper) {
        this.resumeBuilder = resumeBuilder;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает запрос на построение резюме.
     *
     * @param resumeJson JSON-объект с данными резюме.
     * @return Ответ со сгенерированным резюме в формате JSON и статусом 201 Created.
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<JsonNode> buildResume(@RequestBody JsonNode resumeJson) {
        if (resumeJson instanceof TextNode && resumeJson.textValue().isEmpty()) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("message", "Тело запроса не может быть пустой строкой.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorNode);
        }

        final JsonNode generatedResume = resumeBuilder.processResume(resumeJson);
        return ResponseEntity.status(HttpStatus.CREATED).body(generatedResume);
    }
}
