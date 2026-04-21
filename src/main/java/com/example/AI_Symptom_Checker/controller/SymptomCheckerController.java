package com.example.AI_Symptom_Checker.controller;

import com.example.AI_Symptom_Checker.dto.SymptomAnalysisRequest;
import com.example.AI_Symptom_Checker.dto.SymptomAnalysisResponse;
import com.example.AI_Symptom_Checker.service.SymptomAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/symptom-checker")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class SymptomCheckerController {

    private final SymptomAnalysisService symptomAnalysisService;

    @PostMapping("/analyze")
    public Mono<ResponseEntity<SymptomAnalysisResponse>> analyzeSymptoms(
            @RequestBody SymptomAnalysisRequest request) {
        log.info("Received symptom analysis request");
        
        return symptomAnalysisService.analyzeSymptoms(request)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.badRequest().build())
            .doOnError(error -> log.error("Error in analyze symptoms: {}", error.getMessage()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Symptom Checker Service is running");
    }
}
