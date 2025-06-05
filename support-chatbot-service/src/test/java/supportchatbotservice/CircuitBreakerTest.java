package supportchatbotservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import supportchatbotservice.domain.ports.AiServicePort;
import supportchatbotservice.infrastructure.client.DeepSeekAiAdapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerTest {
    @Mock
    private AiServicePort aiServicePort;

    @InjectMocks
    private DeepSeekAiAdapter deepSeekAiAdapter;

    @Test
    void testCircuitBreaker() {
        when(aiServicePort.getAiResponse(any()))
                .thenThrow(new RuntimeException());

        final String response = deepSeekAiAdapter.getAiResponse("test");
        assertEquals("AI service is currently unavailable", response);
    }
}