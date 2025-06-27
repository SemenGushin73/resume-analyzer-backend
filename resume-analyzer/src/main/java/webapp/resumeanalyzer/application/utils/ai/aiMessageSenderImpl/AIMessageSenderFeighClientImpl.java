package webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import webapp.resumeanalyzer.application.utils.ai.AIMessageSender;
import webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient.ChatMessage;
import webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient.OpenRouterApiClient;
import webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient.OpenRouterRequest;
import webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient.OpenRouterResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class AIMessageSenderFeighClientImpl implements AIMessageSender {
    
    private final OpenRouterApiClient openRouterApiClient;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model-name}")
    private String aiModel;

    @Override
    public String send(String message) {
        final OpenRouterRequest request = createRequest(message);

        try {
            final OpenRouterResponse response = openRouterApiClient.generateCompletion(
                    "Bearer " + apiKey,
                    aiModel,
                    request
            );
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().getFirst().getMessage().getContent();
            }
            return "не удалось обработать запрос";
        } catch (Exception e) {
            log.error("API error: ", e);
            return "Ошибка: " + e.getMessage();
        }
    }
    
    private OpenRouterRequest createRequest(String message) {
        final OpenRouterRequest request = new OpenRouterRequest(aiModel, List.of(new ChatMessage("user", message)));
        return request;
    }

}
