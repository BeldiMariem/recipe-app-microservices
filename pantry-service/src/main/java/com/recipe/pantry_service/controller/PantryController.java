package com.recipe.pantry_service.controller;


import com.recipe.pantry_service.entity.PantryItem;
import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.service.PantryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pantry")
public class PantryController {
    
    @Autowired
    private PantryService pantryService;
    
    @PostMapping("/items")
    public ResponseEntity<PantryItem> addItem(
            @RequestBody AddPantryItemRequest request,
            @RequestHeader("X-User-Id") String userId) {
        PantryItem item = pantryService.addItem(userId, request);
        return ResponseEntity.ok(item);
    }
    
    @GetMapping("/items")
    public ResponseEntity<List<PantryItem>> getUserPantry(
            @RequestHeader("X-User-Id") String userId) {
        List<PantryItem> items = pantryService.getUserPantry(userId);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/items/expiring")
    public ResponseEntity<List<PantryItem>> getExpiringItems(
            @RequestHeader("X-User-Id") String userId) {
        List<PantryItem> items = pantryService.getExpiringSoon(userId);
        return ResponseEntity.ok(items);
    }
}