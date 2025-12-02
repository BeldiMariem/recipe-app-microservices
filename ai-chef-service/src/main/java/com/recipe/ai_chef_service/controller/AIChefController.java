package com.recipe.ai_chef_service.controller;

import com.recipe.ai_chef_service.dto.RecipeGenerationRequest;
import com.recipe.ai_chef_service.dto.RecipeGenerationResponse;
import com.recipe.ai_chef_service.service.AIChefService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChefController {
    
    private AIChefService aiChefService;
    private static final Logger log = LoggerFactory.getLogger(AIChefController.class);
    
    @Autowired
    public void setAiChefService(AIChefService aiChefService) {
        this.aiChefService = aiChefService;
    }
    @PostMapping("/generate-recipes")
    public Mono<ResponseEntity<RecipeGenerationResponse>> generateRecipes(
            @Valid @RequestBody RecipeGenerationRequest request,
            @RequestHeader("User-Id") String userId) {
        
        log.info("Generating recipes for user: {}", userId);
        request.setUserId(userId);
        
        try {
            RecipeGenerationResponse response = aiChefService.generateRecipes(request);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Error generating recipes: {}", e.getMessage());
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }
    
    @GetMapping("/quick-suggestions")
    public Mono<ResponseEntity<RecipeGenerationResponse>> getQuickSuggestions(
            @RequestHeader("User-Id") String userId,
            @RequestParam(value = "mealType", required = false) String mealType,
            @RequestParam(value = "maxTime", required = false) Integer maxTime) {
        
        log.info("Getting quick suggestions for user: {}", userId);
        
        RecipeGenerationRequest request = new RecipeGenerationRequest();
        request.setUserId(userId);
        request.setMealType(mealType);
        request.setMaxPreparationTime(maxTime);
        
        try {
            RecipeGenerationResponse response = aiChefService.generateRecipes(request);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Error getting quick suggestions: {}", e.getMessage());
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }
    
    @GetMapping("/use-it-up")
    public Mono<ResponseEntity<RecipeGenerationResponse>> getUseItUpRecipes(
            @RequestHeader("User-Id") String userId) {
        
        log.info("Getting 'use-it-up' recipes for user: {}", userId);
        
        RecipeGenerationRequest request = new RecipeGenerationRequest();
        request.setUserId(userId);
        request.setMealType("any");
        request.setDifficulty("easy");
        
        try {
            RecipeGenerationResponse response = aiChefService.generateRecipes(request);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Error getting use-it-up recipes: {}", e.getMessage());
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }
}