package com.recipe.ai_chef_service.dto;

import java.util.List;

public class AIRecipeRequest {
    private List<PantryItemDTO> pantryItems;
    private RecipeGenerationRequest preferences;

    public AIRecipeRequest() {
    }

    public AIRecipeRequest(List<PantryItemDTO> pantryItems, RecipeGenerationRequest preferences) {
        this.pantryItems = pantryItems;
        this.preferences = preferences;
    }

    public List<PantryItemDTO> getPantryItems() {
        return pantryItems;
    }

    public RecipeGenerationRequest getPreferences() {
        return preferences;
    }

    public void setPantryItems(List<PantryItemDTO> pantryItems) {
        this.pantryItems = pantryItems;
    }

    public void setPreferences(RecipeGenerationRequest preferences) {
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "AIRecipeRequest{" +
                "pantryItems=" + pantryItems +
                ", preferences=" + preferences +
                '}';
    }
}