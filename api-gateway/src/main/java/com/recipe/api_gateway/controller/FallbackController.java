package com.recipe.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "User service is temporarily unavailable. Please try again later.");
        response.put("status", "SERVICE_UNAVAILABLE");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/pantry-service")
    public ResponseEntity<Map<String, String>> pantryServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pantry service is temporarily unavailable.");
        response.put("status", "SERVICE_UNAVAILABLE");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}