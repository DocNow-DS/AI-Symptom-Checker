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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomAnalysisService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiApiUrl;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.builder().build();

    public Mono<SymptomAnalysisResponse> analyzeSymptoms(SymptomAnalysisRequest request) {
        String prompt = buildPrompt(request);

        GeminiRequest geminiRequest = new GeminiRequest(
            List.of(new GeminiRequest.Content(List.of(new GeminiRequest.Part(getSystemPrompt() + "\n\n" + prompt))))
        );

        String url = geminiApiUrl + "/" + model + ":generateContent";

        return webClient.post()
            .uri(url)
            .header("x-goog-api-key", geminiApiKey)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(geminiRequest)
            .retrieve()
            .bodyToMono(GeminiResponse.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                .filter(e -> e instanceof WebClientResponseException.TooManyRequests)
                .doBeforeRetry(signal -> log.warn("Rate limited by Gemini API, retrying... attempt {}", signal.totalRetries() + 1)))
            .map(this::parseAIResponse)
            .onErrorResume(e -> {
                log.error("Error calling Gemini API after retries: {}", e.getMessage());
                return Mono.just(createFallbackResponse());
            });
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

    private SymptomAnalysisResponse parseAIResponse(GeminiResponse geminiResponse) {
        try {
            String content = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
            if (content.contains("```json")) {
                int start = content.indexOf("```json") + 7;
                int end = content.indexOf("```", start);
                if (end > start) {
                    content = content.substring(start, end);
                }
            } else if (content.contains("```")) {
                int start = content.indexOf("```") + 3;
                int end = content.indexOf("```", start);
                if (end > start) {
                    content = content.substring(start, end);
                }
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
