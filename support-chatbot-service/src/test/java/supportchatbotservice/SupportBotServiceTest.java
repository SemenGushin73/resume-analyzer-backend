package supportchatbotservice;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import supportchatbotservice.domain.exception.AiServiceException;
import supportchatbotservice.domain.model.BotResponse;
import supportchatbotservice.domain.model.ChatMessage;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.domain.ports.FaqServicePort;
import supportchatbotservice.domain.service.SupportBotService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SupportBotServiceTest {

    private AiServicePort aiService;
    private FaqServicePort faqService;
    private SupportBotService supportBotService;

    @BeforeEach
    void setup() {
        aiService = mock(AiServicePort.class);
        faqService = mock(FaqServicePort.class);
        supportBotService = new SupportBotService(aiService, faqService);
    }

    @Test
    void testFaqAnswerReturned() {
        when(faqService.findAnswer("faq")).thenReturn(Optional.of("FAQ ответ"));
        final BotResponse response = supportBotService.processMessage(new ChatMessage("faq", "session-1"));
        assertEquals("FAQ ответ", response.getContent());
        assertTrue(response.isFallback());
    }

    @Test
    void testAiAnswerReturned() {
        when(faqService.findAnswer("вопрос")).thenReturn(Optional.empty());
        when(aiService.getAiResponse("вопрос")).thenReturn(Optional.of("AI ответ"));
        final BotResponse response = supportBotService.processMessage(new ChatMessage("вопрос", "session-1"));
        assertEquals("AI ответ", response.getContent());
        assertFalse(response.isFallback());
    }

    @Test
    void testAiServiceThrowsException() {
        when(faqService.findAnswer("ошибка")).thenReturn(Optional.empty());
        when(aiService.getAiResponse("ошибка")).thenThrow(new AiServiceException("Ошибка"));
        final BotResponse response = supportBotService.processMessage(new ChatMessage("ошибка", "session-1"));
        assertEquals("Сервис поддержки временно недоступен, повторите позже", response.getContent());
        assertTrue(response.isFallback());
    }

    @Test
    void testValidationExceptionThrownOnEmptyMessage() {
        final ChatMessage msg = new ChatMessage(" ", "session-1");
        assertThrows(IllegalArgumentException.class, msg::validate);
    }
}