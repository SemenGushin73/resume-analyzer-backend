package ru.kata.project.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных между API Gateway и Resume-Analyzer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private String resumeText;
    private String jobDescription;
}
