package com.recipe.ai_chef_service.client;

import com.recipe.ai_chef_service.dto.PantryItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "pantry-service")
public interface PantryServiceClient {
    
    @GetMapping("/api/pantry/items")
    List<PantryItemDTO> getUserPantry(@RequestHeader("User-Id") String userId);
    
    @GetMapping("/api/pantry/items/expiring")
    List<PantryItemDTO> getExpiringItems(@RequestHeader("User-Id") String userId);
}