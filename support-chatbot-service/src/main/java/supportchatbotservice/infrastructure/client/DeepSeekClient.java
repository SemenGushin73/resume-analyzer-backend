package supportchatbotservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import supportchatbotservice.application.dto.DeepSeekRequest;
import supportchatbotservice.application.dto.DeepSeekResponse;

@FeignClient(
        name = "deepseek-client",
        url = "${deepseek.api.url:https://api.deepseek.com/v1}",
        configuration = DeepSeekConfig.class
)
public interface DeepSeekClient {

    @PostMapping("/v1/chat/completions")
    DeepSeekResponse askAI(@RequestBody DeepSeekRequest request);
}