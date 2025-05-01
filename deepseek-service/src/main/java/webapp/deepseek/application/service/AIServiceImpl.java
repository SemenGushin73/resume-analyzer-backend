package webapp.deepseek.application.service;

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
        final String response = aiMessageSender.send( prompt );
        return response;
    }

    @Override
    public String analyzeResumeScore(String resumeText) {
        final String prompt = promtptGenerator.createPromptForScoring(resumeText);
        final String response = aiMessageSender.send( prompt );
        return response;
    }

    @Override
    public String generateResume(String template, String departmentDescription) {
        final String prompt = promtptGenerator.createPromptForGeneration(template, departmentDescription);
        final String response = aiMessageSender.send( prompt );
        return response;
    }
}
