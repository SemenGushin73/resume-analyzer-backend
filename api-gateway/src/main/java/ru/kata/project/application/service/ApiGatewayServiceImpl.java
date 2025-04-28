package ru.kata.project.application.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kata.project.domain.model.ResumeWithTemplate;
import ru.kata.project.domain.service.ApiGatewayService;
import ru.kata.project.model.Department;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;

/**
 * Сервис для получения Резюме с соответствующими Шаблонами
 */
@Service
public class ApiGatewayServiceImpl implements ApiGatewayService {

    public <T> Mono<List<T>> makeRequest(String uri, Class<T> elementClass) {
        final WebClient webClient = WebClient.create();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(elementClass)
                .collectList();
    }

    public Mono<List<ResumeWithTemplate>> getListOfResumeWithTemplates(Department department) {

        final Mono<List<TemplateDTO>> templates = makeRequest("http://localhost:8765/generator/templates/all?department=" + department, TemplateDTO.class);
        final Mono<List<ResumeDto>> resumes = makeRequest("http://localhost:8765/analyzer/resumes/all?department=" + department, ResumeDto.class);

        return Mono.zip(resumes, templates)
                .map(tuple -> {
                    final List<ResumeWithTemplate> result = new ArrayList<>();
                    for (ResumeDto resume : tuple.getT1()) {
                        final ResumeWithTemplate resumeWithTemplate = new ResumeWithTemplate(resume);
                        for (TemplateDTO template : tuple.getT2()) {
                            resumeWithTemplate.addTemplate(template);
                        }
                        result.add(resumeWithTemplate);
                    }
                    return result;
                });
    }

    /**
     * Метод для создания резюме, отправляя данные в сервис Resume-Builder.
     *
     * @param resumeJson Данные резюме для передачи в Resume-Builder.
     * @return ResponseEntity с результатом обработки, включая статус и тело ответа.
     */
    @Override
    public ResponseEntity<JsonNode> createResume(JsonNode resumeJson) {
        final WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("Content-Type", "application/json")
                .build();

        // Отправляем запрос на resume-analyzer
        final ResponseEntity<JsonNode> buildResume = webClient.post()
                .uri("/resumes")
                .bodyValue(resumeJson)
                .retrieve()
                .toEntity(JsonNode.class)
                .block();

        if (buildResume.getStatusCode().is2xxSuccessful()) {
            // Получаем тело ответа
            final JsonNode responseJson = buildResume.getBody();

            // WebClient для resume-builder
            final WebClient secondWebClient = WebClient.builder()
                    .baseUrl("http://localhost:8082")
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            // Отправляем полученный JSON на resume-builder для обработки
            final ResponseEntity<JsonNode> secondResponse = secondWebClient.post()
                    .uri("/builder") // Укажите путь на втором микросервисе
                    .bodyValue(responseJson)
                    .retrieve()
                    .toEntity(JsonNode.class)
                    .block();

            return ResponseEntity.status(secondResponse.getStatusCode()).body(secondResponse.getBody());
        } else {
            return ResponseEntity.status(buildResume.getStatusCode()).body(buildResume.getBody());
        }
    }
}