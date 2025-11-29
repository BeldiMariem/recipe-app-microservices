package com.recipe.pantry_service.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.dto.PantryItemResponse;
import com.recipe.pantry_service.entity.PantryItem;
import com.recipe.pantry_service.mapper.PantryMapper;
import com.recipe.pantry_service.repository.PantryRepository;

@Service
@Transactional
public class PantryService {
    
    @Autowired
    private PantryRepository pantryRepository;
    
    @Autowired
    private PantryMapper pantryMapper;
    
    public PantryItemResponse addItem(String userId, AddPantryItemRequest request) {
        PantryItem item = pantryMapper.toEntity(request, userId);
        PantryItem savedItem = pantryRepository.save(item);
        return pantryMapper.toResponse(savedItem);
    }
    
    public List<PantryItemResponse> getUserPantry(String userId) {
        List<PantryItem> items = pantryRepository.findByUserId(userId);
        return items.stream()
                .map(pantryMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<PantryItemResponse> getExpiringSoon(String userId) {
        List<PantryItem> items = pantryRepository.findByUserIdAndExpiryDateBefore(
            userId, LocalDate.now().plusDays(3)
        );
        return items.stream()
                .map(pantryMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public PantryItemResponse getItemById(Long itemId, String userId) {
        PantryItem item = pantryRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new RuntimeException("Pantry item not found"));
        return pantryMapper.toResponse(item);
    }
    
    public PantryItemResponse updateItem(Long itemId, AddPantryItemRequest request, String userId) {
        PantryItem item = pantryRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new RuntimeException("Pantry item not found"));

        pantryMapper.updateEntityFromRequest(request, item);
        PantryItem updatedItem = pantryRepository.save(item);
        return pantryMapper.toResponse(updatedItem);
    }

}

  
