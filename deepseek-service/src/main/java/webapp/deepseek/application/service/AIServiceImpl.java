package webapp.deepseek.application.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import webapp.deepseek.utils.ai.AIMessageSender;
import webapp.deepseek.utils.ai.PromptGenerator;

@Service
public class AIServiceImpl implements AIService {

    private final AIMessageSender aiMessageSender;
    private final PromptGenerator promtptGenerator;

    @Autowired
    public AIServiceImpl(AIMessageSender aiMessageSender, PromptGenerator promtptGenerator) {
        this.aiMessageSender = aiMessageSender;
        this.promtptGenerator = promtptGenerator;
    }
    
    @Override
    public String analyzeResume(String resumeText, String jobDescription) {
        final String prompt = promtptGenerator.createPrompt(resumeText, jobDescription);
        final String response = sendPromptAndGetResponse( prompt );
        return response;
    }

    @Override
    public String analyzeResumeScore(String resumeText) {
        final String prompt = promtptGenerator.createPromptForScoring(resumeText);
        final String response = sendPromptAndGetResponse( prompt );
        return response;
    }

    @Override
    public String generateResume(String template, String departmentDescription) {
        final String prompt = promtptGenerator.createPromptForGeneration(template, departmentDescription);
        final String response = sendPromptAndGetResponse( prompt );
        return response;
    }

    private String sendPromptAndGetResponse( String prompt ) {
        try {
            final String response = aiMessageSender.send( prompt );
                    
        if (response == null || response.isBlank()) {
            throw new RuntimeException("Ошибка: AI не смог обработать запрос.");
        }
        
        return response;
        } catch (RuntimeException e) {
            throw new RuntimeException("Ошибка: AI не смог обработать запрос.", e);
        }
    }
}
