package com.recipe.recipe_service.dto;

import com.recipe.recipe_service.entity.Visibility;
import java.util.List;

public class UpdateRecipeRequestDTO {
    private String title;
    private String description;
    private String imageUrl;
    private Integer preparationTime;
    private Integer servings;
    private String difficulty;
    private Visibility visibility;
    private List<RecipeIngredientDTO> ingredients;
    private List<String> instructions;
    
    public UpdateRecipeRequestDTO() {}
    
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
    
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    
    public List<RecipeIngredientDTO> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredientDTO> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
}