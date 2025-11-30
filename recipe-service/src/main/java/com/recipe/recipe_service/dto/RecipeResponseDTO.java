package com.recipe.recipe_service.dto;

import com.recipe.recipe_service.entity.Visibility;
import java.util.List;

public class RecipeResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Integer preparationTime;
    private Integer servings;
    private String difficulty;
    private String userId;
    private Visibility visibility;
    private List<RecipeIngredientDTO> ingredients;
    private List<String> instructions;
    private Double rating;
    private Integer ratingCount;
    
    public RecipeResponseDTO() {}
    
    public RecipeResponseDTO(Long id, String title, String description, String imageUrl, 
                           Integer preparationTime, Integer servings, String difficulty,
                           String userId, Visibility visibility,
                           List<RecipeIngredientDTO> ingredients, List<String> instructions,
                           Double rating, Integer ratingCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.preparationTime = preparationTime;
        this.servings = servings;
        this.difficulty = difficulty;
        this.userId = userId;
        this.visibility = visibility;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    
    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    
    public List<RecipeIngredientDTO> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredientDTO> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
}