package webapp.deepseek.domain.service;

public interface DeepSeekService {
    String analyzeResume(String resumeText, String jobDescription);

    String analyzeResumeScore(String resumeText);

    String generateResume(String template, String departmentDescription);

    public boolean isValidResumeOrJobDescription(String text);
}
