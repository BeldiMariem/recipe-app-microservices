package com.recipe.pantry_service.dto;

import java.time.LocalDate;

public class PantryItemResponse {
    private Long id;
    private String name;
    private Double quantity;
    private String unit;
    private LocalDate expiryDate;
    private LocalDate addedDate;
    private boolean isRunningLow;
    
    public PantryItemResponse() {}
    
    public PantryItemResponse(Long id, String name, Double quantity, String unit, 
                            LocalDate expiryDate, LocalDate addedDate, boolean isRunningLow) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.addedDate = addedDate;
        this.isRunningLow = isRunningLow;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDate getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDate addedDate) { this.addedDate = addedDate; }
    public boolean isRunningLow() { return isRunningLow; }
    public void setRunningLow(boolean runningLow) { isRunningLow = runningLow; }
}