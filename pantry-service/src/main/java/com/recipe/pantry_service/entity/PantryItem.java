package com.recipe.pantry_service.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pantry_items")
public class PantryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userId;
    private String name;
    private Double quantity;
    private String unit;
    private LocalDate expiryDate;
    private LocalDate addedDate;
    private boolean isRunningLow;
    
    public PantryItem() {}
    
    public static PantryItemBuilder builder() {
        return new PantryItemBuilder();
    }
    
    public static class PantryItemBuilder {
        private String userId;
        private String name;
        private Double quantity;
        private String unit;
        private LocalDate expiryDate;
        
        public PantryItemBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public PantryItemBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public PantryItemBuilder quantity(Double quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public PantryItemBuilder unit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public PantryItemBuilder expiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }
        
        public PantryItem build() {
            PantryItem item = new PantryItem();
            item.userId = this.userId;
            item.name = this.name;
            item.quantity = this.quantity;
            item.unit = this.unit;
            item.expiryDate = this.expiryDate;
            item.addedDate = LocalDate.now();
            item.isRunningLow = false;
            return item;
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
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