package supportchatbotservice.application.utils;

/**
 * Шаблоны промптов для AI-сервиса.
 */
public class PromptTemplates {
    public static final String SUPPORT_PROMPT = """
            Ты — профессиональный помощник службы поддержки IT-компании.
            Отвечай строго по теме и максимально кратко (1-2 предложения).
            Если вопрос не связан с техподдержкой — скажи, что не можешь помочь.
            
            Вопрос: %s
            
            Ответ:""";

    public static String formatPrompt(String userMessage) {

        return String.format(SUPPORT_PROMPT, userMessage);

    }
}