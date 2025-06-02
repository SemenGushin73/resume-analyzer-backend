package supportchatbotservice.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.application.dto.DeepSeekRequest;

@Service
@RequiredArgsConstructor
public class DeepSeekAiAdapter implements AiServicePort {
    private final DeepSeekClient deepSeekClient;

    @Override
    @CircuitBreaker(name = "deepseek", fallbackMethod = "fallback")
    public String getAiResponse(String message) {
        return deepSeekClient.askAI(new DeepSeekRequest(message)).getAnswer();
    }

    private String fallback(String message, Exception ex) {
        return "AI service is currently unavailable";
    }
}