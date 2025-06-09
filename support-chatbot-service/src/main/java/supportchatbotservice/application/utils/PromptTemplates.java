package supportchatbotservice.application.utils;

/**
 * Шаблоны промптов для AI-сервиса.
 */
public class PromptTemplates {
    public static final String SUPPORT_PROMPT = """
            Ты - виртуальный помощник службы поддержки. 
            Отвечай вежливо и профессионально.
            Вопрос: %s
            Ответ:""";

    public static String formatPrompt(String userMessage) {
        return String.format(SUPPORT_PROMPT, userMessage);
    }
}