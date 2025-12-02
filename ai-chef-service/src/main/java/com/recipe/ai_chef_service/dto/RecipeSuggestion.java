package com.recipe.ai_chef_service.dto;

import java.util.List;

public class RecipeSuggestion {
    private String title;
    private String description;
    private List<IngredientDTO> ingredients;
    private List<String> instructions;
    private Integer preparationTime;
    private Integer servings;
    private String difficulty;
    private String cuisine;
    private Double confidenceScore; // 0.0 to 1.0
    private List<String> missingIngredients;

    public RecipeSuggestion() {
    }

    public RecipeSuggestion(String title, String description, List<IngredientDTO> ingredients, 
                          List<String> instructions, Integer preparationTime, Integer servings, 
                          String difficulty, String cuisine, Double confidenceScore, 
                          List<String> missingIngredients) {
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.preparationTime = preparationTime;
        this.servings = servings;
        this.difficulty = difficulty;
        this.cuisine = cuisine;
        this.confidenceScore = confidenceScore;
        this.missingIngredients = missingIngredients;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<IngredientDTO> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public Integer getServings() {
        return servings;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCuisine() {
        return cuisine;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIngredients(List<IngredientDTO> ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public void setPreparationTime(Integer preparationTime) {
        this.preparationTime = preparationTime;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public void setMissingIngredients(List<String> missingIngredients) {
        this.missingIngredients = missingIngredients;
    }

    // Helper methods
    public boolean hasMissingIngredients() {
        return missingIngredients != null && !missingIngredients.isEmpty();
    }

    public int getMissingIngredientCount() {
        return missingIngredients != null ? missingIngredients.size() : 0;
    }

    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.7;
    }

    public boolean isMediumConfidence() {
        return confidenceScore != null && confidenceScore >= 0.4 && confidenceScore < 0.7;
    }

    public boolean isLowConfidence() {
        return confidenceScore != null && confidenceScore < 0.4;
    }

    // toString
    @Override
    public String toString() {
        return "RecipeSuggestion{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", instructions=" + instructions +
                ", preparationTime=" + preparationTime +
                ", servings=" + servings +
                ", difficulty='" + difficulty + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", missingIngredients=" + missingIngredients +
                '}';
    }
}