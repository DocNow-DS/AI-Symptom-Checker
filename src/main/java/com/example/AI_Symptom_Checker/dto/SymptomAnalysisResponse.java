package com.example.AI_Symptom_Checker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomAnalysisResponse {
    private String summary;
    private List<String> possibleConditions;
    private List<DoctorSpecialtyRecommendation> recommendedSpecialties;
    private List<String> selfCareTips;
    private String urgencyLevel;
    private String disclaimer;
    private String followUpQuestions;
}
