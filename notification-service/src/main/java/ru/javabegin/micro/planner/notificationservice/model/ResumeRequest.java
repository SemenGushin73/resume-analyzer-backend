package ru.javabegin.micro.planner.notificationservice.model;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class ResumeRequest {
    private String requestId;
    private String resumeText;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumeRequest that = (ResumeRequest) o;
        return Objects.equals(requestId, that.requestId) && Objects.equals(resumeText, that.resumeText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, resumeText);
    }
}
