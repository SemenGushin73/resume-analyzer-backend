package webapp.resumegenerator.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import webapp.resumegenerator.domain.service.ResumeBuilder;

/**
 * Сервис для построения резюме.
 * Этот класс получает JSON-данные резюме, выполняет обработку (например, добавляет недостающие поля)
 * и возвращает итоговое сгенерированное резюме.
 */
@Service
public class ResumeBuilderServiceImpl implements ResumeBuilder {

    @Override
    public JsonNode processResume(JsonNode resume) {
        return resume;
    }
}