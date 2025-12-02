package com.recipe.ai_chef_service.dto;

import java.util.List;

public class RecipeGenerationResponse {
    private List<RecipeSuggestion> suggestions;
    private String generationId;
    private Long timestamp;

    public RecipeGenerationResponse() {
    }

    public RecipeGenerationResponse(List<RecipeSuggestion> suggestions) {
        this.suggestions = suggestions;
        this.generationId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public RecipeGenerationResponse(List<RecipeSuggestion> suggestions, String generationId, Long timestamp) {
        this.suggestions = suggestions;
        this.generationId = generationId;
        this.timestamp = timestamp;
    }

    // Getters
    public List<RecipeSuggestion> getSuggestions() {
        return suggestions;
    }

    public String getGenerationId() {
        return generationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setSuggestions(List<RecipeSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public void setGenerationId(String generationId) {
        this.generationId = generationId;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // Helper methods
    public int getRecipeCount() {
        return suggestions != null ? suggestions.size() : 0;
    }

    // toString
    @Override
    public String toString() {
        return "RecipeGenerationResponse{" +
                "suggestions=" + suggestions +
                ", generationId='" + generationId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}