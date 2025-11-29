package com.recipe.pantry_service.dto;

import java.time.LocalDate;

public class AddPantryItemRequest {
    private String name;
    private Double quantity;
    private String unit;
    private LocalDate expiryDate;
    
    public AddPantryItemRequest() {}
    
    public AddPantryItemRequest(String name, Double quantity, String unit, LocalDate expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}