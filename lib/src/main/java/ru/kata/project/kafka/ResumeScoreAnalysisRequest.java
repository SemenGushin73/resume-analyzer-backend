package ru.kata.project.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.resumeDto.ResumeDto;


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
