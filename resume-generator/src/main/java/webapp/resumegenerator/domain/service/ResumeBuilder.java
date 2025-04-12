package webapp.resumegenerator.domain.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Интерфейс для построения резюме.
 * Определяет методы для обработки JSON-данных резюме и их преобразования в сгенерированное резюме.
 */
public interface ResumeBuilder {

    JsonNode processResume(JsonNode resumeJson);
}
