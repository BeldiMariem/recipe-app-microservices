package com.recipe.pantry_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        try {
            PantryItemResponse item = pantryService.getItemById(itemId, userId);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/items/updateItem/{itemId}")
    public ResponseEntity<PantryItemResponse> updateItem(
            @PathVariable Long itemId,
            @RequestBody AddPantryItemRequest request,
            @RequestHeader("User-Id") String userId) {
        try {
            PantryItemResponse response = pantryService.updateItem(itemId, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}