package ru.kata.project.kafka.config;

/**
 * Топики для сообщений в kafka
 */
public class KafkaTopics {

    public static final String RESUME_ANALYZE_REQUESTS = "resume-analyze-requests";

    public static final String RESUME_ANALYZE_RESULTS = "resume-analyze-results";

    public static final String AI_RESUME_SCORE_REQUESTS = "ai-resume-score-requests";

    public static final String AI_RESUME_SCORE_RESULTS = "ai-resume-score-results";

    public static final String RESUME_GENERATION_REQUESTS = "resume-generation-requests";

    public static final String DEEPSEEK_GENERATION_REQUESTS = "deepseek-generation-requests";

    private KafkaTopics() {
    }
}
