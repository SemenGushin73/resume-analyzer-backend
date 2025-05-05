package ru.kata.project.application.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
@Slf4j
public class ApiGatewayServiceImpl implements ApiGatewayService {

    private final WebClient analyzerClient;
    private final WebClient builderClient;

    public ApiGatewayServiceImpl(WebClient.Builder webClientBuilder) {
        this.analyzerClient = webClientBuilder
                .baseUrl("http://resume-analyzer:8083")
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.builderClient = webClientBuilder
                .baseUrl("http://resume-generator:8082")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

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
     * @param resumeDto Данные резюме для передачи в Resume-Builder.
     * @return Mono<ResponseEntity> с результатом обработки, включая статус и тело ответа.
     */
    @Override
    public Mono<ResponseEntity<JsonNode>> createResume(ResumeDto resumeDto) {
        log.info("Sending resume JSON to resume-analyzer");

        return analyzerClient.post()
                .uri("/resumes")
                .bodyValue(resumeDto)
                .retrieve()
                .toEntity(JsonNode.class)
                .flatMap(buildResume -> {
                    JsonNode responseJson = buildResume.getBody();

                    if (responseJson == null || responseJson.isEmpty()) {
                        return Mono.error(new RestClientException("resume-analyzer returned an empty response"));
                    }

                    log.info("Sending analyzed resume to resume-builder");

                    return builderClient.post()
                            .uri("/builder")
                            .bodyValue(responseJson)
                            .retrieve()
                            .toEntity(JsonNode.class);
                });
    }
}
