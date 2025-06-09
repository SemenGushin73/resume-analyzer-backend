package supportchatbotservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import supportchatbotservice.application.dto.ChatRequestDTO;
import supportchatbotservice.application.dto.ChatResponseDTO;
import supportchatbotservice.domain.model.BotResponse;
import supportchatbotservice.domain.model.ChatMessage;
import supportchatbotservice.domain.ports.MessageSenderPort;
import supportchatbotservice.domain.service.SupportBotService;

import java.security.Principal;

/**
 * Сервис для обработки сообщений пользователя и координации работы чат-бота.
 */
@Service
@RequiredArgsConstructor
public class ChatBotAppService {
    private final SupportBotService supportBotService;
    private final MessageSenderPort messageSender;

    public void handleUserMessage(ChatRequestDTO request, Principal principal) {
        try {
            final BotResponse response = supportBotService.processMessage(
                    new ChatMessage(request.getMessage(), request.getSessionId())
            );

            messageSender.sendResponse(
                    principal.getName(),
                    new ChatResponseDTO(response.getContent(), response.isFallback())
            );
        } catch (Exception ex) {
            messageSender.sendError(principal.getName(), ex.getMessage());
        }
    }
}