package ru.javabegin.micro.planner.notificationservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class ResumeRequest {

    @Schema(description = "Текст резюме",
            example = "Java developer with Spring experience.")
    private String resumeText;


    @Schema(description = "Описание вакансии",
            example = "Looking for a backend developer with Java 21, Kafka and PostgreSQL knowledge.")
    private String jobDescription;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumeRequest that = (ResumeRequest) o;
        return Objects.equals(resumeText, that.resumeText) && Objects.equals(jobDescription, that.jobDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resumeText, jobDescription);
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
