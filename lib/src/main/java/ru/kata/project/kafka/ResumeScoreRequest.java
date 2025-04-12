package ru.kata.project.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи запроса на оценку резюме между API Gateway и resume-analyser.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeScoreRequest {
    private String resumeId;
}
