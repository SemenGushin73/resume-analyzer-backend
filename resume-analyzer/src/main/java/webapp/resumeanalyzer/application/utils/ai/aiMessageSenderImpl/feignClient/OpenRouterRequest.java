package webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient;

import lombok.Data;

import java.util.List;

/**
 * Тело запроса к OpenRouter API
 * Содержит имя модели и историю диалога с ии
 */
@Data
public class OpenRouterRequest {
    private String model;
    private List<ChatMessage> messages;
}
