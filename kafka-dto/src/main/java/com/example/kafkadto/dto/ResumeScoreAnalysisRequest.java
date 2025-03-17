package com.example.kafkadto.dto;

import com.example.kafkadto.domain.model.ResumeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeScoreAnalysisRequest {
    private String resumeId;
    private ResumeDto resume;
}
