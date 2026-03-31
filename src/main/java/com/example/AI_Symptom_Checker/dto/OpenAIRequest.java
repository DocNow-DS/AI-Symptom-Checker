package com.example.AI_Symptom_Checker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;
    private int max_tokens;
}
