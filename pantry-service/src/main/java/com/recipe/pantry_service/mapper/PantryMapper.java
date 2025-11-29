package com.recipe.pantry_service.mapper;

import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.dto.PantryItemResponse;
import com.recipe.pantry_service.entity.PantryItem;
import org.springframework.stereotype.Component;

@Component
public class PantryMapper {
    
    public PantryItem toEntity(AddPantryItemRequest request, String userId) {
        if (request == null) {
            return null;
        }
        
        // Use your existing builder for the main fields
        PantryItem item = PantryItem.builder()
            .userId(userId)
            .name(request.getName() != null ? request.getName().toLowerCase() : null)
            .quantity(request.getQuantity())
            .unit(request.getUnit())
            .expiryDate(request.getExpiryDate())
            .build();
            
        return item;
    }
    
    public PantryItemResponse toResponse(PantryItem entity) {
        if (entity == null) {
            return null;
        }
        
        PantryItemResponse response = new PantryItemResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setExpiryDate(entity.getExpiryDate());
        response.setAddedDate(entity.getAddedDate());
        response.setRunningLow(entity.isRunningLow());
        
        return response;
    }
    
    public void updateEntityFromRequest(AddPantryItemRequest request, PantryItem entity) {
        if (request == null || entity == null) {
            return;
        }
        
        entity.setName(request.getName() != null ? request.getName().toLowerCase() : null);
        entity.setQuantity(request.getQuantity());
        entity.setUnit(request.getUnit());
        entity.setExpiryDate(request.getExpiryDate());
    }
}