package webapp.deepseek.aiStuff;

public class ValidResumeChecker {
    public boolean isValidResumeOrJobDescription(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        final String lowerCaseText = text.toLowerCase();

        return lowerCaseText.contains("опыт работы") ||
               lowerCaseText.contains("навыки") ||
               lowerCaseText.contains("образование") ||
               lowerCaseText.contains("должностные обязанности") ||
               lowerCaseText.contains("требования") ||
               lowerCaseText.contains("резюме") ||
               lowerCaseText.contains("вакансия");
    }
}
