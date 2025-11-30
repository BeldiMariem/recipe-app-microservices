package com.recipe.recipe_service.dto;

public class RecipeIngredientDTO {
    private String name;
    private Double quantity;
    private String unit;
    
    public RecipeIngredientDTO() {}
    
    public RecipeIngredientDTO(String name, Double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}