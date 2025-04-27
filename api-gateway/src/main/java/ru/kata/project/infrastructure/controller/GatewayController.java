package ru.kata.project.infrastructure.controller;

import ru.kata.project.application.service.KafkaConsumerService;
import ru.kata.project.application.service.KafkaProducerService;
import ru.kata.project.domain.service.ApiGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kata.project.kafka.ResumeScoreResult;


/**
 * Контроллер для управления запросами в API Gateway.
 */
@RestController
@RequestMapping("/api")
public class GatewayController {

    private final ApiGatewayService apiGatewayService;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);


    @Autowired
    public GatewayController(ApiGatewayService apiGatewayService,
                             KafkaProducerService kafkaProducerService,
                             WebClient.Builder webClientBuilder,
                             KafkaConsumerService kafkaConsumerService) {
        this.apiGatewayService = apiGatewayService;
        this.kafkaProducerService = kafkaProducerService;
        this.kafkaConsumerService = kafkaConsumerService;
        this.webClient = webClientBuilder.baseUrl("http://deepseek-service:9090/").build();
    }

    /**
     * Обработчик GET-запроса.
     * @return строка "OK", указывающая, что шлюз работает корректно.
     */
    @GetMapping("/status")
    public String status() {
        return "OK";
    }

    /**
     * Маршрутизация запроса на анализ резюме в deepseek-service
     *
     * @param resumeText текст резюме, который нужно проанализировать
     * @param jobDescription описание вакансии требуемое контроллером deepseek-service
     * @return возвращает результат работы deepseek-service
     */
    @PostMapping("/ai/analyze")
    public Mono<ResponseEntity<String>> analyzeResume(
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobDescription") String jobDescription) {

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/resume/analyze")
                        .queryParam("resumeText", resumeText)
                        .queryParam("jobDescription", jobDescription)
                        .build())
                .retrieve()
                .onStatus(status -> status.value() >= 400 && status.value() < 500, response -> {
                    log.error("Ошибка 4xx: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Ошибка в запросе"));
                })
                .onStatus(status -> status.value() >= 500, response -> {
                    log.error("Ошибка 5xx: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Ошибка на сервере"));
                })
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Ошибка анализа резюме: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Ошибка при анализе резюме: " + e.getMessage()));
                });
    }

    /**
     * Эндпоинт для отправки запроса на оценку резюме.
     * Принимает resumeId и отправляет сообщение в Kafka.
     */
    @PostMapping("ai/resume-score")
    public ResponseEntity<Void> requestResumeScore(@RequestParam String resumeId) {
        if (resumeId == null) {
            return ResponseEntity.badRequest().build();
        } else {
            kafkaProducerService.sendScoreRequest(resumeId);
            return ResponseEntity.accepted().build();
        }
    }

    /**
     * Эндпоинт для получения результата оценки резюме.
     * Клиент опрашивает статус или получает готовый результат.
     */
    @GetMapping("/ai/resume-score")
    public ResponseEntity<ResumeScoreResult> getResumeScore(@RequestParam String resumeId) {
        final ResumeScoreResult result = kafkaConsumerService.getScoreResult(resumeId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }


    /**
     * Добавляет маршрут для создания нового резюме.
     * Этот метод будет обрабатывать POST-запросы для создания резюме.
     *
     * @param resumeJson Данные для создания резюме, включая личные данные кандидата, образование и опыт.
     * @return ResponseEntity с результатом обработки запроса.
     */
    @PostMapping("/api/resume/create")
    public ResponseEntity<JsonNode> createResume(@RequestBody JsonNode resumeJson) {
        return apiGatewayService.createResume(resumeJson);
    }
}
