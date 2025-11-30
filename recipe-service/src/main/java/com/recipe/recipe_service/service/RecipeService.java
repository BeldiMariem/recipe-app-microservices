package com.recipe.recipe_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.recipe_service.dto.CreateRecipeRequestDTO;
import com.recipe.recipe_service.dto.PantryItem;
import com.recipe.recipe_service.dto.RecipeResponseDTO;
import com.recipe.recipe_service.dto.UpdateRecipeRequestDTO;
import com.recipe.recipe_service.entity.Recipe;
import com.recipe.recipe_service.entity.Visibility;
import com.recipe.recipe_service.mapper.RecipeMapper;
import com.recipe.recipe_service.repository.RecipeRepository;

import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class RecipeService {
    
    @Autowired
    private RecipeRepository recipeRepository;
    
    @Autowired
    private PantryServiceClient pantryServiceClient;
    
    @Autowired
    private RecipeMapper recipeMapper;
    
    public List<RecipeResponseDTO> getRecipeSuggestions(String userId) {
        List<PantryItem> pantryItems = pantryServiceClient.getUserPantry(userId);
        List<String> availableIngredients = pantryItems.stream()
            .map(PantryItem::getName)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        List<Recipe> recipes;
        if (availableIngredients.isEmpty()) {
            recipes = recipeRepository.findVisibleRecipes(userId);
        } else {
            recipes = recipeRepository.findByIngredientNamesAndUser(availableIngredients, userId);
        }
        
        return recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<RecipeResponseDTO> getUseItUpRecipes(String userId) {
        List<PantryItem> expiringItems = pantryServiceClient.getExpiringItems(userId);
        List<String> expiringIngredients = expiringItems.stream()
            .map(PantryItem::getName)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        List<Recipe> recipes;
        if (expiringIngredients.isEmpty()) {
            recipes = recipeRepository.findVisibleRecipes(userId);
        } else {
            recipes = recipeRepository.findByIngredientNamesAndUser(expiringIngredients, userId);
        }
        
        return recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<RecipeResponseDTO> getAllRecipes(String userId) {
        List<Recipe> recipes = recipeRepository.findVisibleRecipes(userId);
        return recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<RecipeResponseDTO> getPublicRecipes() {
        List<Recipe> recipes = recipeRepository.findPublicRecipes();
        return recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<RecipeResponseDTO> getMyRecipes(String userId) {
        List<Recipe> recipes = recipeRepository.findByUserId(userId);
        return recipes.stream()
                .map(recipeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public RecipeResponseDTO getRecipeById(Long id, String userId) {
        Recipe recipe = recipeRepository.findById(id).orElse(null);
        if (recipe != null && (recipe.getVisibility() == Visibility.PUBLIC || recipe.getUserId().equals(userId))) {
            return recipeMapper.toResponseDTO(recipe);
        }
        return null;
    }
    
    public RecipeResponseDTO createRecipe(CreateRecipeRequestDTO request, String userId) {
        Recipe recipe = recipeMapper.toEntity(request);
        recipe.setUserId(userId);
        if (recipe.getVisibility() == null) {
            recipe.setVisibility(Visibility.PUBLIC);
        }
        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toResponseDTO(savedRecipe);
    }
    
    public RecipeResponseDTO updateRecipe(Long id, UpdateRecipeRequestDTO request, String userId) {
        Recipe existingRecipe = recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recipe not found or you don't have permission to update it"));
        
        recipeMapper.updateEntityFromRequest(request, existingRecipe);
        Recipe updatedRecipe = recipeRepository.save(existingRecipe);
        return recipeMapper.toResponseDTO(updatedRecipe);
    }
    
    public void deleteRecipe(Long id, String userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recipe not found or you don't have permission to delete it"));
        recipeRepository.delete(recipe);
    }
    
    @PostConstruct
    public void initSampleData() {
        if (recipeRepository.count() == 0) {
            Recipe pasta = new Recipe("1", "Tomato Pasta", "Simple and delicious tomato pasta", 20, 2, "EASY", Visibility.PUBLIC);
            pasta.addIngredient("pasta", 200.0, "grams");
            pasta.addIngredient("tomato", 4.0, "pieces");
            pasta.addIngredient("garlic", 2.0, "cloves");
            pasta.addIngredient("olive oil", 2.0, "tablespoons");
            pasta.addInstruction("Cook pasta according to package instructions");
            pasta.addInstruction("Chop tomatoes and garlic");
            pasta.addInstruction("Sauté garlic in olive oil, add tomatoes");
            pasta.addInstruction("Mix with cooked pasta and serve");
            recipeRepository.save(pasta);
            
            Recipe secretRecipe = new Recipe("2", "Secret Family Recipe", "A private family recipe", 30, 4, "MEDIUM", Visibility.PUBLIC);
            secretRecipe.addIngredient("chicken", 500.0, "grams");
            secretRecipe.addIngredient("secret sauce", 1.0, "bottle");
            secretRecipe.addInstruction("Marinate chicken with secret sauce");
            secretRecipe.addInstruction("Cook for 25 minutes");
            secretRecipe.addInstruction("Serve hot");
            recipeRepository.save(secretRecipe);
            
            Recipe salad = new Recipe("1", "Green Salad", "Fresh green salad", 10, 1, "EASY", Visibility.PUBLIC);
            salad.addIngredient("lettuce", 1.0, "head");
            salad.addIngredient("tomato", 2.0, "pieces");
            salad.addIngredient("cucumber", 1.0, "piece");
            salad.addIngredient("olive oil", 1.0, "tablespoon");
            salad.addInstruction("Wash and chop all vegetables");
            salad.addInstruction("Mix in a bowl");
            salad.addInstruction("Drizzle with olive oil and serve");
            recipeRepository.save(salad);
        }
    }
}