package com.example.AI_Symptom_Checker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSpecialtyRecommendation {
    private String specialty;
    private String reason;
    private int priority;
}
