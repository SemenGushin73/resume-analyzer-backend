package ru.kata.project.domain.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.kata.project.resumeDto.ResumeDto;


/**
 * Интерфейс сервиса для отправки запросов в микросервисы.
 */
public interface ApiGatewayService {

    <T> Mono<List<T>> makeRequest(String uri, Class<T> elementClass);

    Mono<ResponseEntity<JsonNode>> createResume(ResumeDto resumeDto);
}

