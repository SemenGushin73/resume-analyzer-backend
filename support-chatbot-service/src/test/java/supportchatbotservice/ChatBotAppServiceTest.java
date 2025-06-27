package supportchatbotservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import supportchatbotservice.application.service.ChatBotAppService;
import supportchatbotservice.domain.model.BotResponse;
import supportchatbotservice.domain.model.ChatMessage;
import supportchatbotservice.domain.ports.MessageSenderPort;
import supportchatbotservice.domain.service.SupportBotService;
import ru.kata.project.chatbotDTO.ChatRequestDTO;
import ru.kata.project.chatbotDTO.ChatResponseDTO;

import java.security.Principal;

import static org.mockito.Mockito.*;

public class ChatBotAppServiceTest {

    private SupportBotService supportBotService;
    private MessageSenderPort messageSender;
    private ChatBotAppService chatBotAppService;

    @BeforeEach
    void setUp() {
        supportBotService = mock(SupportBotService.class);
        messageSender = mock(MessageSenderPort.class);
        chatBotAppService = new ChatBotAppService(supportBotService, messageSender);
    }

    @Test
    void testSuccessfulUserMessageHandling() {
        final ChatRequestDTO request = ChatRequestDTO.create("Привет", "s1");
        final Principal principal = () -> "user";

        when(supportBotService.processMessage(any(ChatMessage.class)))
                .thenReturn(new BotResponse("Ответ", false));

        chatBotAppService.handleUserMessage(request, principal);

        verify(messageSender).sendResponse(eq("s1"),
                eq(new ChatResponseDTO("Ответ", false)));
    }

    @Test
    void testFallbackOnServiceException() {
        final ChatRequestDTO request = ChatRequestDTO.create("Ошибка", "s2");
        final Principal principal = () -> "user";

        when(supportBotService.processMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("AI error"));

        chatBotAppService.handleUserMessage(request, principal);

        verify(messageSender).sendError(eq("s2"), contains("AI error"));
    }
}