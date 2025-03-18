package com.example.kafkadto.dto;

import com.example.kafkadto.domain.model.ResumeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи запроса на оценку резюме между resume-analyser и deepseek-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeScoreAnalysisRequest {
    private String resumeId;
    private ResumeDto resume;
}
