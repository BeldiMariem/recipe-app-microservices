package com.recipe.recipe_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.recipe_service.dto.CreateRecipeRequestDTO;
import com.recipe.recipe_service.dto.RecipeResponseDTO;
import com.recipe.recipe_service.dto.UpdateRecipeRequestDTO;
import com.recipe.recipe_service.service.RecipeService;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    
    @Autowired
    private RecipeService recipeService;
    
    @GetMapping("/suggestions")
    public ResponseEntity<List<RecipeResponseDTO>> getRecipeSuggestions(@RequestHeader("User-Id") String userId) {
        List<RecipeResponseDTO> suggestions = recipeService.getRecipeSuggestions(userId);
        return ResponseEntity.ok(suggestions);
    }
    
    @GetMapping("/use-it-up")
    public ResponseEntity<List<RecipeResponseDTO>> getUseItUpRecipes(@RequestHeader("User-Id") String userId) {
        List<RecipeResponseDTO> recipes = recipeService.getUseItUpRecipes(userId);
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<RecipeResponseDTO>> getAllRecipes(@RequestHeader("User-Id") String userId) {
        List<RecipeResponseDTO> recipes = recipeService.getAllRecipes(userId);
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<RecipeResponseDTO>> getPublicRecipes() {
        List<RecipeResponseDTO> recipes = recipeService.getPublicRecipes();
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/my-recipes")
    public ResponseEntity<List<RecipeResponseDTO>> getMyRecipes(@RequestHeader("User-Id") String userId) {
        List<RecipeResponseDTO> recipes = recipeService.getMyRecipes(userId);
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/getRecipeById/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipeById(@PathVariable Long id, @RequestHeader("User-Id") String userId) {
        RecipeResponseDTO recipe = recipeService.getRecipeById(id, userId);
        if (recipe != null) {
            return ResponseEntity.ok(recipe);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/createRecipe")
    public ResponseEntity<RecipeResponseDTO> createRecipe(@RequestBody CreateRecipeRequestDTO request, @RequestHeader("User-Id") String userId) {
        RecipeResponseDTO savedRecipe = recipeService.createRecipe(request, userId);
        return ResponseEntity.ok(savedRecipe);
    }
    
    @PutMapping("/updateRecipe/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(@PathVariable Long id, @RequestBody UpdateRecipeRequestDTO request, @RequestHeader("User-Id") String userId) {
        try {
            RecipeResponseDTO updatedRecipe = recipeService.updateRecipe(id, request, userId);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/deleteRecipe/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id, @RequestHeader("User-Id") String userId) {
        try {
            recipeService.deleteRecipe(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}