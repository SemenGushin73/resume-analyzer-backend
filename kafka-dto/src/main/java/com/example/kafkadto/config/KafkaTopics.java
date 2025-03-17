package com.example.kafkadto.config;

public class KafkaTopics {

    public static final String RESUME_ANALYZE_REQUESTS = "resume-analyze-requests";

    public static final String DEEPSEEK_ANALYSIS_REQUESTS = "deepseek-analysis-requests";

    public static final String RESUME_ANALYZE_RESULTS = "resume-analyze-results";

    public static final String DEEPSEEK_GENERATION_RESULT = "deepseek-generation-result";

    public static final String AI_RESUME_SCORE_REQUESTS = "ai-resume-score-requests";

    public static final String AI_RESUME_SCORE_RESULTS = "ai-resume-score-results";

    private KafkaTopics() {
    }
}
