package supportchatbotservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import supportchatbotservice.application.dto.ChatRequestDTO;
import supportchatbotservice.application.dto.ChatResponseDTO;
import supportchatbotservice.application.service.ChatBotAppService;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.domain.ports.FaqServicePort;
import supportchatbotservice.domain.ports.MessageSenderPort;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatBotServiceTest {

    @Mock
    private AiServicePort aiServicePort;

    @Mock
    private FaqServicePort faqServicePort;

    @Mock
    private MessageSenderPort messageSenderPort;

    @Mock
    private Principal principal;

    @InjectMocks
    private ChatBotAppService chatBotAppService;

    @Test
    void whenAiAvailable_thenReturnAiResponse() {
        final ChatRequestDTO request = ChatRequestDTO.create("test", "session123");
        when(aiServicePort.getAiResponse(any())).thenReturn("Ответ от AI");
        when(faqServicePort.findAnswer(any())).thenReturn(Optional.empty());

        chatBotAppService.handleUserMessage(request, principal);

        verify(messageSenderPort).sendResponse(eq(principal.getName()), any(ChatResponseDTO.class));
    }

    @Test
    void whenAiFails_thenReturnFallbackResponse() {
        final ChatRequestDTO request = ChatRequestDTO.create("test", "session123");
        when(aiServicePort.getAiResponse(any())).thenThrow(new RuntimeException());
        when(faqServicePort.findAnswer(any())).thenReturn(Optional.of("Fallback"));

        chatBotAppService.handleUserMessage(request, principal);

        verify(messageSenderPort).sendResponse(eq(principal.getName()), any(ChatResponseDTO.class));
    }
}