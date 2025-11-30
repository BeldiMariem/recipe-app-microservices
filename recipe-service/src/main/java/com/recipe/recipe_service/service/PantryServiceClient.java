package com.recipe.recipe_service.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.recipe.recipe_service.dto.PantryItem;

@Service
public class PantryServiceClient {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    public List<PantryItem> getUserPantry(String userId) {
        try {
            return webClientBuilder.build()
                .get()
                .uri("http://pantry-service/api/pantry/items/getItems")
                .header("User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PantryItem>>() {})
                .block();
        } catch (Exception e) {
            System.out.println("Error fetching pantry: " + e.getMessage());
            return List.of(); 
        }
    }
    
    public List<PantryItem> getExpiringItems(String userId) {
        try {
            return webClientBuilder.build()
                .get()
                .uri("http://pantry-service/api/pantry/items/expiring")
                .header("User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PantryItem>>() {})
                .block();
        } catch (Exception e) {
            System.out.println("Error fetching expiring items: " + e.getMessage());
            return List.of();
        }
    }
}

