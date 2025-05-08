package ru.javabegin.micro.planner.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javabegin.micro.planner.notificationservice.model.ResumeRequest;



import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/analyze")
@Tag(name = "Контроллер клиента.", description = "Осуществляет HTTP-запрос через Swagger")
public class ResumeAnalysisController {

    private final KafkaTemplate<String, ResumeRequest> kafkaTemplate;

    @Autowired
    public ResumeAnalysisController(KafkaTemplate<String, ResumeRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Operation(
            summary = "Отправка http запроса через Swagger",
            description = """
                    Запрос пробрасывается в Kafka (produce).
                    Немедленно возвращается 202 Accepted.
                    """
    )
    @ApiResponse(responseCode = "202", description = "Запрос ушел в Kafka",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResumeRequest.class)))
    @PostMapping()
    public ResponseEntity<Void> analyzeResume(@RequestBody ResumeRequest request) {
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);


        kafkaTemplate.send("RESUME_ANALYZE_REQUESTS", requestId, request);

        // Немедленно возвращаем 202 Accepted и requestId
        return ResponseEntity
                .accepted()
                .header("X-Request-ID", requestId)
                .build();
    }
}
