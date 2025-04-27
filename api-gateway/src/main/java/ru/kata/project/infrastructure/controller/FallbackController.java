package ru.kata.project.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/analyzer")
    public ResponseEntity<Map<String, Object>> analyzerFallback() {
        final Map<String, Object> response = new HashMap<>();
        response.put("message", "Сервис resume-analyzer временно недоступен");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/generator")
    public ResponseEntity<Map<String, Object>> generatorFallback() {
        final Map<String, Object> response = new HashMap<>();
        response.put("message", "Сервис resume-generator временно недоступен");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
