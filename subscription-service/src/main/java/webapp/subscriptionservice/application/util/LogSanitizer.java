package webapp.subscriptionservice.application.util;

public final class LogSanitizer {

    private LogSanitizer() {
    }

    /**
     * Вспомогательный метод для очистки строк от символов возврата каретки и перевода строки.
     * Используется для предотвращения CRLF-инъекций в логах и метаданных.
     *
     * @param input Исходная строка.
     * @return Очищенная строка, где '\n' и '\r' заменены на '_'.
     */
    public static String sanitizeLogInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replace('\n', '_').replace('\r', '_');
    }
}