package supportchatbotservice.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import supportchatbotservice.domain.exception.AiServiceException;
import supportchatbotservice.domain.model.BotResponse;
import supportchatbotservice.domain.model.ChatMessage;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.domain.ports.FaqServicePort;

import java.util.Optional;

/**
 * Сервис для обработки сообщений поддержки, объединяющий FAQ и AI-сервисы.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupportBotService {
    private final AiServicePort aiService;
    private final FaqServicePort faqService;

    public BotResponse processMessage(ChatMessage message) {
        message.validate();

        final Optional<String> faqAnswer = faqService.findAnswer(message.getContent());
        if (faqAnswer.isPresent()) {
            return new BotResponse(faqAnswer.get(), true);
        }

        try {
            final String aiResponse = aiService.getAiResponse(message.getContent()).orElse("Ответ не получен");
            if (aiResponse == null || aiResponse.isBlank()) {
                throw new AiServiceException("Empty response from AI");
            }
            return new BotResponse(aiResponse, false);
        } catch (Exception ex) {
            log.error("AI service error: {}", ex.getMessage());
            return new BotResponse("Сервис поддержки временно недоступен, повторите позже", true);
        }
    }
}