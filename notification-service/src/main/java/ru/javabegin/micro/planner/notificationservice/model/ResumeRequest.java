package ru.javabegin.micro.planner.notificationservice.model;

import lombok.Data;

@Data
public class ResumeRequest {
    private String requestId;
    private String resumeText;
}
