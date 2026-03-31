package com.example.AI_Symptom_Checker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAnalysisRequest {
    private String symptoms;
    private String age;
    private String gender;
    private String duration;
    private String severity;
}
