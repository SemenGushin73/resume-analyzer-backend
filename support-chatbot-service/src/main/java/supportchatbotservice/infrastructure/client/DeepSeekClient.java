package supportchatbotservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import supportchatbotservice.application.dto.DeepSeekRequest;
import supportchatbotservice.application.dto.DeepSeekResponse;

@FeignClient(
        name = "deepseek-client",
        url = "https://openrouter.ai/api/v1",
        configuration = DeepSeekConfig.class,
        fallback = DeepSeekClientFallback.class
)
public interface DeepSeekClient {

    @PostMapping("/chat/completions")
    DeepSeekResponse askAI(@RequestBody DeepSeekRequest request);
}