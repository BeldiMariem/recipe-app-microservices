package com.recipe.ai_chef_service.dto;

import java.time.LocalDate;

public class PantryItemDTO {
    private Long id;
    private String userId;
    private String name;
    private Double quantity;
    private String unit;
    private LocalDate expiryDate;
    private LocalDate addedDate;
    private boolean runningLow;

    public PantryItemDTO() {
    }

    public PantryItemDTO(String name, Double quantity, String unit, LocalDate expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public PantryItemDTO(Long id, String userId, String name, Double quantity, String unit, 
                        LocalDate expiryDate, LocalDate addedDate, boolean runningLow) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.addedDate = addedDate;
        this.runningLow = runningLow;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getAddedDate() {
        return addedDate;
    }

    public boolean isRunningLow() {
        return runningLow;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setAddedDate(LocalDate addedDate) {
        this.addedDate = addedDate;
    }

    public void setRunningLow(boolean runningLow) {
        this.runningLow = runningLow;
    }

    // toString
    @Override
    public String toString() {
        return "PantryItemDTO{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", expiryDate=" + expiryDate +
                ", addedDate=" + addedDate +
                ", runningLow=" + runningLow +
                '}';
    }
}