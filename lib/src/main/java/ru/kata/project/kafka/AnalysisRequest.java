package ru.kata.project.kafka;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных между API Gateway и Resume-Analyzer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для передачи данных между API Gateway и Resume-Analyzer")
public class AnalysisRequest {

    @Schema(description = "Id резюме")
    private String requestId;

    @Schema(description = "Текст резюме",
            example = "Java developer with Spring experience.")
    private String resumeText;

    @Schema(description = "Описание вакансии",
            example = "Looking for a backend developer with Java 21, Kafka and PostgreSQL knowledge.")
    private String jobDescription;

    public AnalysisRequest(String resumeText, String jobDescription) {
        this.jobDescription = jobDescription;
        this.resumeText = resumeText;

    }

}
