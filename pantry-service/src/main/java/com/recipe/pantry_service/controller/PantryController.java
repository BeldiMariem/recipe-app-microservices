package com.recipe.pantry_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.dto.PantryItemResponse;
import com.recipe.pantry_service.service.PantryService;

@RestController
@RequestMapping("/api/pantry")
public class PantryController {
    
    @Autowired
    private PantryService pantryService;
    
    @PostMapping("/items/addItem")
    public ResponseEntity<PantryItemResponse> addItem(
            @RequestBody AddPantryItemRequest request,
            @RequestHeader("User-Id") String userId) {
        PantryItemResponse response = pantryService.addItem(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/items/getItems")
    public ResponseEntity<List<PantryItemResponse>> getUserPantry(
            @RequestHeader("User-Id") String userId) {
        List<PantryItemResponse> items = pantryService.getUserPantry(userId);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/items/expiring")
    public ResponseEntity<List<PantryItemResponse>> getExpiringItems(
            @RequestHeader("User-Id") String userId) {
        List<PantryItemResponse> items = pantryService.getExpiringSoon(userId);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/items/getItemById/{itemId}")
    public ResponseEntity<PantryItemResponse> getItemById(
            @PathVariable Long itemId,
            @RequestHeader("User-Id") String userId) {
        PantryItemResponse item = pantryService.getItemById(itemId, userId);
        return ResponseEntity.ok(item);
    }
    
    @PutMapping("/items/updateItem/{itemId}")
    public ResponseEntity<PantryItemResponse> updateItem(
            @PathVariable Long itemId,
            @RequestBody AddPantryItemRequest request,
            @RequestHeader("User-Id") String userId) {
        PantryItemResponse response = pantryService.updateItem(itemId, request, userId);
        return ResponseEntity.ok(response);
    }
}