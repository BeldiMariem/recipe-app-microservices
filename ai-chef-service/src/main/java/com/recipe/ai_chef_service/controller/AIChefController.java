package com.recipe.ai_chef_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.recipe.ai_chef_service.dto.RecipeGenerationRequest;
import com.recipe.ai_chef_service.dto.RecipeGenerationResponse;
import com.recipe.ai_chef_service.service.AIChefService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AIChefController {
    
    private static final Logger log = LoggerFactory.getLogger(AIChefController.class);
    
    private final AIChefService aiChefService;
    
    public AIChefController(AIChefService aiChefService) {
        this.aiChefService = aiChefService;
    }
    
    @PostMapping("/generate-recipes")
    public ResponseEntity<RecipeGenerationResponse> generateRecipes(
            @Valid @RequestBody RecipeGenerationRequest request,
            @RequestHeader("User-Id") String userId) {
        
        log.info("Generating recipes for user: {}", userId);
        request.setUserId(userId);
        
        try {
            RecipeGenerationResponse response = aiChefService.generateRecipes(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating recipes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/quick-suggestions")
    public ResponseEntity<RecipeGenerationResponse> getQuickSuggestions(
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
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting quick suggestions: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/use-it-up")
    public ResponseEntity<RecipeGenerationResponse> getUseItUpRecipes(
            @RequestHeader("User-Id") String userId) {
        
        log.info("Getting 'use-it-up' recipes for user: {}", userId);
        
        RecipeGenerationRequest request = new RecipeGenerationRequest();
        request.setUserId(userId);
        request.setMealType("any");
        request.setDifficulty("easy");
        
        try {
            RecipeGenerationResponse response = aiChefService.generateRecipes(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting use-it-up recipes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}