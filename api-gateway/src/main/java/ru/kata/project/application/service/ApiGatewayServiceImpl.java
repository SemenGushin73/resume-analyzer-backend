package ru.kata.project.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import ru.kata.project.subscriptionDto.CreateSubscriptionRequest;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;

/**
 * Сервис для получения Резюме с соответствующими Шаблонами
 */
@Service
@Slf4j
public class ApiGatewayServiceImpl implements ApiGatewayService {

    private final WebClient analyzerClient;
    private final WebClient builderClient;
    private final WebClient subscriptionClient;

    public ApiGatewayServiceImpl(WebClient.Builder webClientBuilder) {
        this.analyzerClient = webClientBuilder
                .baseUrl("http://resume-analyzer:8083")
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.builderClient = webClientBuilder
                .baseUrl("http://resume-generator:8082")
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.subscriptionClient = webClientBuilder
                .baseUrl("http://subscription-service:8086")
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
                    final JsonNode responseJson = buildResume.getBody();

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

    /**
     * Метод для создания подписки, отправляет данные в subscription-service.
     * @param request содержит userId и тип подписки.
     * @return Mono, содержащий ResponseEntity с PaymentSessionResponseDto,
     * включающим данные для инициации платежной сессии.
     */
    @Override
    public Mono<ResponseEntity<PaymentSessionResponseDto>> createSubscription(CreateSubscriptionRequest request) {
        log.info("Проксируем запрос на создание подписки для userId: {}, type: {}", request.getUserId(), request.getType());
        return subscriptionClient.post()
                .uri("/internal/subscriptions/create")
                .bodyValue(request)
                .retrieve()
                .toEntity(PaymentSessionResponseDto.class);
    }

    /**
     * Метод для отмены активной подписки пользователя в subscription-service.
     * @param userId UUID пользователя, чья подписка должна быть отменена.
     * @return Mono, содержащий ResponseEntity со строковым сообщением о результате отмены.
     */
    @Override
    public Mono<ResponseEntity<String>> cancelSubscription(UUID userId) {
        log.info("Проксируем запрос на отмену подписки для userId: {}", userId);
        return subscriptionClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/subscriptions/cancel")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .toEntity(String.class);
    }

    /**
     * Метод на получение истории подписок пользователя в subscription-service.
     * @param userId UUID пользователя, чью историю подписок необходимо получить.
     * @return Mono, содержащий ResponseEntity со списком SubscriptionHistoryDto.
     */
    @Override
    public Mono<ResponseEntity<List<SubscriptionHistoryDto>>> getSubscriptionHistory(UUID userId) {
        log.info("Проксируем запрос на получение истории подписок для userId: {}", userId);
        return subscriptionClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/subscriptions/history")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .toEntityList(SubscriptionHistoryDto.class);
    }

    /**
     * Метод на получение активной подписки пользователя в subscription-service.
     * @param userId UUID пользователя, чью активную подписку необходимо получить.
     * @return Mono, содержащий ResponseEntity с UserSubscriptionDto, показывающий активную подписку.
     */
    @Override
    public Mono<ResponseEntity<UserSubscriptionDto>> getActiveSubscription(UUID userId) {
        log.info("Проксируем запрос на получение активной подписки для userId: {}", userId);
        return subscriptionClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/subscriptions/active")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .toEntity(UserSubscriptionDto.class);
    }
}
