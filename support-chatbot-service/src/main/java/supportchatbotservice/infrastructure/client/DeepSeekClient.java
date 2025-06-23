package supportchatbotservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import supportchatbotservice.application.dto.DeepSeekRequestDTO;
import supportchatbotservice.application.dto.DeepSeekResponseDTO;


/**
 * Feign-клиент для DeepSeek API.
 */
@FeignClient(
        name = "deepseek-client",
        url = "${openrouter.base-url}",
        configuration = DeepSeekConfig.class
)
public interface DeepSeekClient {

    @PostMapping("/v1/chat/completions")
    DeepSeekResponseDTO askAI(@RequestBody DeepSeekRequestDTO request);
}