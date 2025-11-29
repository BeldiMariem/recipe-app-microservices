package com.recipe.pantry_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.entity.PantryItem;
import com.recipe.pantry_service.repository.PantryRepository;

@Service
@Transactional
public class PantryService {
    
    @Autowired
    private PantryRepository pantryRepository;
    
    public PantryItem addItem(String userId, AddPantryItemRequest request) {
        PantryItem item = PantryItem.builder()
            .userId(userId)
            .name(request.getName().toLowerCase())
            .quantity(request.getQuantity())
            .unit(request.getUnit())
            .expiryDate(request.getExpiryDate())
            .build();
            
        return pantryRepository.save(item);
    }
    
    public List<PantryItem> getUserPantry(String userId) {
        return pantryRepository.findByUserId(userId);
    }
    
    public List<PantryItem> getExpiringSoon(String userId) {
        return pantryRepository.findByUserIdAndExpiryDateBefore(
            userId, LocalDate.now().plusDays(3)
        );
    }
}