package com.example.kafkadto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для передачи результата оценки резюме от DeepSeek в API Gateway.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeScoreResult {

    @JsonProperty("resume_id")
    private String resumeId;

    @JsonProperty("resume_score")
    private int score;

    @JsonProperty("feedback")
    private List<String> feedback;
}