//package ru.kata.project.infrastructure.controller;
//
//import com.example.kafkadto.config.KafkaTopics;
//import com.example.kafkadto.dto.AnalysisRequest;
//import com.example.kafkadto.dto.AnalysisResult;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//import webapp.apigateway.application.service.ApiGatewayServiceImpl;
//import webapp.apigateway.application.service.KafkaConsumerService;
//import webapp.apigateway.application.service.KafkaProducerService;
//import webapp.apigateway.domain.model.Department;
//import webapp.apigateway.domain.model.ResumeWithTemplate;
//
//import java.util.List;
//import java.util.UUID;
//
///**
// * Контроллер для управления запросами в API Gateway.
// */
//@RestController
//@RequestMapping("/api")
//public class GatewayController {
//
//    private ApiGatewayServiceImpl apiGatewayServiceImpl;
//
//    private final KafkaTemplate<String, AnalysisRequest> kafkaTemplate;
//    private final KafkaConsumerService kafkaConsumerService;
//    private final ObjectMapper objectMapper;
//
//    @Autowired
//    public GatewayController(ApiGatewayServiceImpl apiGatewayServiceImpl, KafkaTemplate<String, AnalysisRequest> kafkaTemplate, KafkaConsumerService kafkaConsumerService, ObjectMapper objectMapper) {
//        this.apiGatewayServiceImpl = apiGatewayServiceImpl;
//        this.kafkaTemplate = kafkaTemplate;
//
//        this.kafkaConsumerService = kafkaConsumerService;
//        this.objectMapper = objectMapper;
//    }
//
//    /**
//     * Обработчик GET-запроса.
//     *
//     * @return строка "OK", указывающая, что шлюз работает корректно.
//     */
//    @GetMapping("/status")
//    public String status() {
//        return "OK";
//    }
//
//    /**
//     * Обработчик GET-запроса.
//     *
//     * @return Список всех резюме с соответствующим шаблоном.
//     */
////    @GetMapping("/resumes-with-templates")
////    public ResponseEntity<Mono<List<ResumeWithTemplate>>> getAllResume(
////            @RequestParam(value = "department", required = false) Department department) {
////        if (department == null) {
////            return ResponseEntity.ok(apiGatewayServiceImpl.getListOfResumeWithTemplates());
////        } else {
////            return ResponseEntity.ok(apiGatewayServiceImpl.getListOfResumeWithTemplates(department));
////        }
////    }
//    @PostMapping("/ai/analyze")
//    public ResponseEntity<String> analyzeResume(@RequestBody AnalysisRequest data) {
//        if (data == null || data.getResumeText() == null || data.getJobDescription() == null) {
//            return ResponseEntity.badRequest().body("Invalid request data.");
//        }
//
//        String requestId = UUID.randomUUID().toString();
//        kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, data);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Request accepted. ID: " + requestId);
//    }
//
//    @GetMapping("/ai/status/{requestId}")
//    public ResponseEntity<?> getAnalysisResult(@PathVariable String requestId) {
//        AnalysisResult result = kafkaConsumerService.getResult(requestId);
//
//        if (result != null) {
//            return ResponseEntity.ok(result);
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Result not found for ID: " + requestId);
//        }
//    }
//}