package com.example.kafkadto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных между API Gateway и Resume-Analyzer
 */
@Data
@NoArgsConstructor
public class AnalysisRequest {
    private String resumeText;
    private String jobDescription;
}
