package ru.kata.project.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи результата анализа от DeepSeek в API Gateway
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String requestId; // Идентификатор для сопоставления запроса/ответа
    private String result;
}
