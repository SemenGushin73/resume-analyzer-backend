package supportchatbotservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kata.project.chatbotDTO.AIRequestDTO;
import ru.kata.project.chatbotDTO.AIResponseDTO;


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
    AIResponseDTO askAI(@RequestBody AIRequestDTO request);
}