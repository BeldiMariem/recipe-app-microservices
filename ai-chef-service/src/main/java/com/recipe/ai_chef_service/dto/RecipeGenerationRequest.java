package com.recipe.ai_chef_service.dto;

import java.util.List;

public class RecipeGenerationRequest {
    private String userId;
    private List<String> preferredCuisines;
    private String mealType; // breakfast, lunch, dinner, snack
    private Integer maxPreparationTime; // in minutes
    private Integer servings;
    private String difficulty; // easy, medium, hard
    private List<String> dietaryRestrictions; // vegetarian, vegan, gluten-free, etc.
    private List<String> excludeIngredients;

    public RecipeGenerationRequest() {
    }

    public RecipeGenerationRequest(String userId, List<String> preferredCuisines, String mealType, 
                                  Integer maxPreparationTime, Integer servings, String difficulty, 
                                  List<String> dietaryRestrictions, List<String> excludeIngredients) {
        this.userId = userId;
        this.preferredCuisines = preferredCuisines;
        this.mealType = mealType;
        this.maxPreparationTime = maxPreparationTime;
        this.servings = servings;
        this.difficulty = difficulty;
        this.dietaryRestrictions = dietaryRestrictions;
        this.excludeIngredients = excludeIngredients;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public List<String> getPreferredCuisines() {
        return preferredCuisines;
    }

    public String getMealType() {
        return mealType;
    }

    public Integer getMaxPreparationTime() {
        return maxPreparationTime;
    }

    public Integer getServings() {
        return servings;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public List<String> getExcludeIngredients() {
        return excludeIngredients;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPreferredCuisines(List<String> preferredCuisines) {
        this.preferredCuisines = preferredCuisines;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public void setMaxPreparationTime(Integer maxPreparationTime) {
        this.maxPreparationTime = maxPreparationTime;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public void setExcludeIngredients(List<String> excludeIngredients) {
        this.excludeIngredients = excludeIngredients;
    }

    // Optional: Helper methods
    public boolean hasPreferredCuisines() {
        return preferredCuisines != null && !preferredCuisines.isEmpty();
    }

    public boolean hasDietaryRestrictions() {
        return dietaryRestrictions != null && !dietaryRestrictions.isEmpty();
    }

    public boolean hasExcludeIngredients() {
        return excludeIngredients != null && !excludeIngredients.isEmpty();
    }

    // toString method
    @Override
    public String toString() {
        return "RecipeGenerationRequest{" +
                "userId='" + userId + '\'' +
                ", preferredCuisines=" + preferredCuisines +
                ", mealType='" + mealType + '\'' +
                ", maxPreparationTime=" + maxPreparationTime +
                ", servings=" + servings +
                ", difficulty='" + difficulty + '\'' +
                ", dietaryRestrictions=" + dietaryRestrictions +
                ", excludeIngredients=" + excludeIngredients +
                '}';
    }
}