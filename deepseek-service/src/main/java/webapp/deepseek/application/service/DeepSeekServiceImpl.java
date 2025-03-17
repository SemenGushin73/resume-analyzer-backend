package webapp.deepseek.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import webapp.deepseek.infrastructure.client.OpenRouterApiClient;
import webapp.deepseek.infrastructure.client.OpenRouterRequest;
import webapp.deepseek.infrastructure.client.OpenRouterResponse;
import webapp.deepseek.domain.ChatMessage;
import webapp.deepseek.domain.service.DeepSeekService;

import java.util.List;

/**
 * • Основной сервис для анализа и генерации резюме.
 * • Получает данные от пользователя, формирует запрос к AI, обрабатывает ответ
 */
@Service
@Slf4j
public class DeepSeekServiceImpl implements DeepSeekService {

    private final OpenRouterApiClient openRouterApiClient;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model-name}")
    private String aiModel;

    public DeepSeekServiceImpl(OpenRouterApiClient openRouterApiClient) {
        this.openRouterApiClient = openRouterApiClient;
    }

    @Override
    public String analyzeResume(String resumeText, String jobDescription) {
        String prompt = createPrompt(resumeText, jobDescription);

        OpenRouterRequest request = createRequest(prompt);

        try {
            OpenRouterResponse response = openRouterApiClient.generateCompletion(
                    "Bearer " + apiKey,
                    aiModel,
                    request
            );
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().getFirst().getMessage().getContent();
            }
            return "Не удалось проанализировать резюме.";
        } catch (Exception e) {
            log.error("API error: ", e);
            return "Ошибка анализа: " + e.getMessage();
        }
    }

    @Override
    public String generateResume(String template, String departmentDescription) {
        String prompt = createPromptForGeneration(template, departmentDescription);

        OpenRouterRequest request = createRequest(prompt);

        try {
            OpenRouterResponse response = openRouterApiClient.generateCompletion(
                    "Bearer " + apiKey,
                    aiModel,
                    request
            );
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().getFirst().getMessage().getContent();
            }
            return "Не удалось проанализировать резюме.";
        } catch (Exception e) {
            log.error("API error: ", e);
            return "Ошибка генерации: " + e.getMessage();
        }
    }

    public String analyzeResumeScore(String resumeText) {
        String prompt = createPromptForScoring(resumeText);

        OpenRouterRequest request = createRequest(prompt);

        try {
            OpenRouterResponse response = openRouterApiClient.generateCompletion(
                    "Bearer " + apiKey,
                    aiModel,
                    request
            );
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().getFirst().getMessage().getContent();
            }
            return "Ошибка: AI не смог обработать запрос.";
        } catch (Exception e) {
            log.error("Ошибка анализа резюме: ", e);
            return "Ошибка анализа: " + e.getMessage();
        }
    }

    @Override
    public boolean isValidResumeOrJobDescription(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerCaseText = text.toLowerCase();

        return lowerCaseText.contains("опыт работы") ||
               lowerCaseText.contains("навыки") ||
               lowerCaseText.contains("образование") ||
               lowerCaseText.contains("должностные обязанности") ||
               lowerCaseText.contains("требования") ||
               lowerCaseText.contains("резюме") ||
               lowerCaseText.contains("вакансия");
    }

    private OpenRouterRequest createRequest(String prompt) {
        OpenRouterRequest request = new OpenRouterRequest();
        request.setModel(aiModel);
        request.setMessages(List.of(new ChatMessage("user", prompt)));
        return request;
    }

    private String createPrompt(String resumeText, String jobDescription) {
        return "Вы полезный помощник, который анализирует резюме на предмет соответствия должности. " +
               "Учитывая следующее резюме и описание должности, дайте оценку (0-100) " +
               "того, насколько хорошо резюме соответствует описанию должности, и объясните свои доводы. " +
               "Resume:\n" + resumeText + "\n\nJob Description:\n" + jobDescription;
    }

    private  String createPromptForGeneration (String template, String departmentDescription) {
        return "Напиши профессиональное и привлекательно резюме на основе полученной от пользователя информации:" +
                template + "и описания отдела на место в котором претендует соискатель" + departmentDescription +
                "результат представь на русском языке в виде готового резюме без своих рассуждений";
    }

    /**
     * Генерирует prompt для AI-анализа резюме (0-100)
     */
    private String createPromptForScoring(String resumeText) {
        return "Руководствуясь параметрами ниже оцени резюме и дай оценку от 0 до 100, а также рекомендации для улучшения.\n" +
                "Структура резюме: есть ли опыт, навыки, образование, хобби, социальные ссылки.\n" +
                "Грамматика и стилистика: Оцени качество языка, наличие грамматических и стилистических ошибок.\n" +
                "Полнота данных: Определи, насколько резюме заполнено, и если необходимо, дай рекомендации по улучшению.\n\n" +
                "На основе анализа выведи результат в формате JSON следующим образом:\n" +
                "{ \"resume_score\": <оценка от 0 до 100>, \"feedback\": [ \"Рекомендация 1\", \"Рекомендация 2\", ... ] }\n" +
                "Количество рекомендаций может быть любым (включая 0 или более двух).\n\n" +
                "Резюме:\n" + resumeText;
    }


}
