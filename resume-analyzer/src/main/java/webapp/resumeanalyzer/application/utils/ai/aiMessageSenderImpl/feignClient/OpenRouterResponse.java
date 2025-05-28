package webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ответ от API OpenRouter
 * Содержит варианты ответов и данные об использовании токенов
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenRouterResponse {
    @Singular
    List<Choice> choices;
    UsageTokens usageTokens;

    public List<Choice> getChoices() {
        return Optional.ofNullable(choices)
                .map(ArrayList::new)
                .map(Collections::unmodifiableList)
                .orElse(Collections.emptyList());
    }

    // Наилучший вариант ответа
    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        Message message;
    }

    // Tекст ответа
    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        String content;
    }

    // Расход токенов. Сколько токенов потрачено на запрос+ответ
    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageTokens {
        int total_tokens;
    }
}