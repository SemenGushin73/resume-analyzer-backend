package webapp.deepseek.application.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import webapp.deepseek.aiStuff.AIMessageSender;
import webapp.deepseek.aiStuff.PromptGenerator;
import webapp.deepseek.domain.service.AIService;

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
        String prompt = promtptGenerator.createPrompt(resumeText, jobDescription);
        String response = aiMessageSender.send( prompt );
        return response;
    }

    @Override
    public String analyzeResumeScore(String resumeText) {
        String prompt = promtptGenerator.createPromptForScoring(resumeText);
        String response = aiMessageSender.send( prompt );
        return response;
    }

    @Override
    public String generateResume(String template, String departmentDescription) {
        String prompt = promtptGenerator.createPromptForGeneration(template, departmentDescription);
        String response = aiMessageSender.send( prompt );
        return response;
    }
}
