package com.example.AI_Symptom_Checker.service;

import com.example.AI_Symptom_Checker.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomAnalysisService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiApiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.builder().build();

    public Mono<SymptomAnalysisResponse> analyzeSymptoms(SymptomAnalysisRequest request) {
        String prompt = buildPrompt(request);
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", getSystemPrompt()));
        messages.add(new ChatMessage("user", prompt));

        OpenAIRequest openAIRequest = new OpenAIRequest(
            model,
            messages,
            0.7,
            1500
        );

        return webClient.post()
            .uri(openAiApiUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(openAIRequest)
            .retrieve()
            .bodyToMono(OpenAIResponse.class)
            .map(this::parseAIResponse)
            .doOnError(error -> log.error("Error calling OpenAI API: {}", error.getMessage()));
    }

    private String getSystemPrompt() {
        return """ 
You are a medical AI assistant helping patients understand their symptoms and find appropriate medical care.
Your role is to:
1. Analyze symptoms described by patients
2. Suggest possible conditions (with clear disclaimers that these are NOT diagnoses)
3. Recommend appropriate doctor specialties to consult
4. Provide general self-care advice when appropriate
5. Identify urgency levels and advise when emergency care is needed

IMPORTANT DISCLAIMERS TO INCLUDE:
- This is NOT a medical diagnosis
- Always consult a qualified healthcare professional
- Seek emergency care for severe symptoms

Respond in JSON format with these fields:
{
  "summary": "brief summary of the symptom analysis",
  "possibleConditions": ["condition1", "condition2"],
  "recommendedSpecialties": [
    {"specialty": "Specialty Name", "reason": "why this specialty", "priority": 1}
  ],
  "selfCareTips": ["tip1", "tip2"],
  "urgencyLevel": "LOW|MEDIUM|HIGH|EMERGENCY",
  "disclaimer": "full medical disclaimer text",
  "followUpQuestions": "questions to ask the patient for better assessment"
}
""";
    }

    private String buildPrompt(SymptomAnalysisRequest request) {
        return String.format("""
Please analyze the following patient symptoms:

Symptoms: %s
Age: %s
Gender: %s
Duration: %s
Severity: %s

Provide a preliminary assessment following medical safety guidelines.
""",
            request.getSymptoms(),
            request.getAge(),
            request.getGender(),
            request.getDuration(),
            request.getSeverity()
        );
    }

    private SymptomAnalysisResponse parseAIResponse(OpenAIResponse openAIResponse) {
        try {
            String content = openAIResponse.getChoices().get(0).getMessage().getContent();
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7, content.lastIndexOf("```"));
            } else if (content.contains("```")) {
                content = content.substring(content.indexOf("```") + 3, content.lastIndexOf("```"));
            }
            content = content.trim();
            
            return objectMapper.readValue(content, SymptomAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            return createFallbackResponse();
        }
    }

    private SymptomAnalysisResponse createFallbackResponse() {
        List<DoctorSpecialtyRecommendation> specialties = new ArrayList<>();
        specialties.add(new DoctorSpecialtyRecommendation("General Practitioner", "For initial assessment and proper diagnosis", 1));

        return SymptomAnalysisResponse.builder()
            .summary("Unable to provide detailed analysis. Please consult a healthcare professional.")
            .possibleConditions(List.of("Unknown - requires professional assessment"))
            .recommendedSpecialties(specialties)
            .selfCareTips(List.of("Monitor your symptoms", "Rest and stay hydrated", "Consult a doctor if symptoms worsen"))
            .urgencyLevel("MEDIUM")
            .disclaimer("This is not a medical diagnosis. Please consult a qualified healthcare professional for proper evaluation.")
            .followUpQuestions("Please describe your symptoms in more detail including when they started and what makes them better or worse.")
            .build();
    }
}
