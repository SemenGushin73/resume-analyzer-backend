package ru.kata.project.domain.service;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.subscriptionDto.CreateSubscriptionRequest;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;


/**
 * Интерфейс сервиса для отправки запросов в микросервисы.
 */
public interface ApiGatewayService {

    <T> Mono<List<T>> makeRequest(String uri, Class<T> elementClass);

    Mono<ResponseEntity<JsonNode>> createResume(ResumeDto resumeDto);

    Mono<ResponseEntity<PaymentSessionResponseDto>> createSubscription(CreateSubscriptionRequest request);

    Mono<ResponseEntity<String>> cancelSubscription(UUID userId);

    Mono<ResponseEntity<List<SubscriptionHistoryDto>>> getSubscriptionHistory(UUID userId);

    Mono<ResponseEntity<UserSubscriptionDto>> getActiveSubscription(UUID userId);
}

