package supportchatbotservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kata.project.chatbotDTO.ChatRequestDTO;
import ru.kata.project.chatbotDTO.ChatResponseDTO;
import supportchatbotservice.domain.model.BotResponse;
import supportchatbotservice.domain.model.ChatMessage;
import supportchatbotservice.domain.ports.MessageSenderPort;
import supportchatbotservice.domain.service.SupportBotService;

import java.security.Principal;

/**
 * Сервис для обработки сообщений пользователя и координации работы чат-бота.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotAppService {
    private final SupportBotService supportBotService;
    private final MessageSenderPort messageSender;

    public void handleUserMessage(ChatRequestDTO request, Principal principal) {
        final String sessionId = request.getSessionId();
        try {
            log.info("WebSocket сообщение получено: {}", request.getMessage());

            final BotResponse response = supportBotService.processMessage(
                    new ChatMessage(request.getMessage(), sessionId)
            );

            log.info("Ответ пользователю [{}]: {} (FAQ: {})", sessionId, response.getContent(), response.isFallback());
            messageSender.sendResponse(sessionId, new ChatResponseDTO(response.getContent(), response.isFallback()));

        } catch (Exception ex) {
            messageSender.sendError(sessionId, ex.getMessage());
        }
    }
}