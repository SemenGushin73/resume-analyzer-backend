package supportchatbotservice.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import supportchatbotservice.domain.model.BotResponse;
import supportchatbotservice.domain.model.ChatMessage;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.domain.ports.FaqServicePort;

@Service
@RequiredArgsConstructor
public class SupportBotService {
    private final AiServicePort aiService;
    private final FaqServicePort faqService;

    public BotResponse processMessage(ChatMessage message) {
        message.validate();

        try {
            final String answer = aiService.getAiResponse(message.getContent());
            return new BotResponse(answer, false);
        } catch (Exception ex) {
            final String fallback = faqService.findAnswer(message.getContent())
                    .orElse("Service is temporarily unavailable");
            return new BotResponse(fallback, true);
        }
    }
}