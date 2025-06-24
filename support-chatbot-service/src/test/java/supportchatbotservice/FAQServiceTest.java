package supportchatbotservice;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import supportchatbotservice.infrastructure.faq.FAQProperties;
import supportchatbotservice.infrastructure.faq.FAQService;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FAQServiceTest {

    private FAQService faqService;

    @BeforeEach
    void setup() {
        FAQProperties props = new FAQProperties();
        HashMap<String, String> map = new HashMap<>();
        map.put("вопрос1", "ответ1");
        map.put("как загрузить", "загрузите PDF");
        props.setAnswers(map);
        faqService = new FAQService(props);
    }

    @Test
    void testFindAnswerExists() {
        Optional<String> result = faqService.findAnswer("загрузить");
        assertTrue(result.isPresent());
        assertEquals("загрузите PDF", result.get());
    }

    @Test
    void testFindAnswerNotExists() {
        Optional<String> result = faqService.findAnswer("несуществующий вопрос");
        assertTrue(result.isEmpty());
    }
}