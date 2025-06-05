package supportchatbotservice.infrastructure.client;


import org.springframework.stereotype.Component;
import supportchatbotservice.application.dto.DeepSeekRequest;
import supportchatbotservice.application.dto.DeepSeekResponse;

@Component
public class DeepSeekClientFallback implements DeepSeekClient {
    @Override
    public DeepSeekResponse askAI(DeepSeekRequest request) {
        return new DeepSeekResponse("Fallback response: Service unavailable");
    }
}