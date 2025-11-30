package com.recipe.recipe_service.service;

import com.recipe.recipe_service.dto.CreateRecipeRequestDTO;
import com.recipe.recipe_service.dto.PantryItem;
import com.recipe.recipe_service.dto.RecipeResponseDTO;
import com.recipe.recipe_service.dto.UpdateRecipeRequestDTO;
import com.recipe.recipe_service.entity.Recipe;
import com.recipe.recipe_service.entity.Visibility;
import com.recipe.recipe_service.mapper.RecipeMapper;
import com.recipe.recipe_service.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private PantryServiceClient pantryServiceClient;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeService recipeService;

    private final String USER_ID = "user123";
    private final String OTHER_USER_ID = "otherUser";

    private Recipe createTestRecipe(String userId, Visibility visibility) {
        Recipe recipe = new Recipe(userId, "Test Recipe", "Test Description", 
                                 30, 4, "EASY", visibility);
        recipe.setId(1L);
        recipe.addIngredient("tomato", 2.0, "pieces");
        recipe.addInstruction("Test instruction");
        return recipe;
    }

    private RecipeResponseDTO createTestRecipeResponse(Long id, String title, String userId, Visibility visibility) {
        RecipeResponseDTO response = new RecipeResponseDTO();
        response.setId(id);
        response.setTitle(title);
        response.setUserId(userId);
        response.setVisibility(visibility);
        return response;
    }

    private CreateRecipeRequestDTO createTestCreateRequest() {
        CreateRecipeRequestDTO request = new CreateRecipeRequestDTO();
        request.setTitle("New Recipe");
        request.setDescription("New Description");
        request.setVisibility(Visibility.PUBLIC);
        return request;
    }

    private UpdateRecipeRequestDTO createTestUpdateRequest() {
        UpdateRecipeRequestDTO request = new UpdateRecipeRequestDTO();
        request.setTitle("Updated Recipe");
        request.setDescription("Updated Description");
        request.setVisibility(Visibility.PRIVATE);
        return request;
    }

    @Test
    void getRecipeSuggestions_WithIngredients_ReturnsMatchingRecipes() {
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        
        PantryItem pantryItem = new PantryItem();
        pantryItem.setName("tomato");
        List<PantryItem> pantryItems = List.of(pantryItem);
        
        when(pantryServiceClient.getUserPantry(USER_ID)).thenReturn(pantryItems);
        when(recipeRepository.findByIngredientNamesAndUser(anyList(), eq(USER_ID)))
                .thenReturn(List.of(testRecipe));
        when(recipeMapper.toResponseDTO(any(Recipe.class))).thenReturn(testRecipeResponse);

        List<RecipeResponseDTO> result = recipeService.getRecipeSuggestions(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getTitle());
        verify(pantryServiceClient).getUserPantry(USER_ID);
        verify(recipeRepository).findByIngredientNamesAndUser(List.of("tomato"), USER_ID);
    }

    @Test
    void getRecipeSuggestions_NoIngredients_ReturnsVisibleRecipes() {
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        
        when(pantryServiceClient.getUserPantry(USER_ID)).thenReturn(List.of());
        when(recipeRepository.findVisibleRecipes(USER_ID)).thenReturn(List.of(testRecipe));
        when(recipeMapper.toResponseDTO(any(Recipe.class))).thenReturn(testRecipeResponse);

        List<RecipeResponseDTO> result = recipeService.getRecipeSuggestions(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipeRepository).findVisibleRecipes(USER_ID);
        verify(recipeRepository, never()).findByIngredientNamesAndUser(anyList(), anyString());
    }

    @Test
    void getUseItUpRecipes_WithExpiringItems_ReturnsMatchingRecipes() {
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        
        PantryItem expiringItem = new PantryItem();
        expiringItem.setName("tomato");
        List<PantryItem> expiringItems = List.of(expiringItem);
        
        when(pantryServiceClient.getExpiringItems(USER_ID)).thenReturn(expiringItems);
        when(recipeRepository.findByIngredientNamesAndUser(anyList(), eq(USER_ID)))
                .thenReturn(List.of(testRecipe));
        when(recipeMapper.toResponseDTO(any(Recipe.class))).thenReturn(testRecipeResponse);

        List<RecipeResponseDTO> result = recipeService.getUseItUpRecipes(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pantryServiceClient).getExpiringItems(USER_ID);
        verify(recipeRepository).findByIngredientNamesAndUser(List.of("tomato"), USER_ID);
    }

    @Test
    void getRecipeById_UserOwnsRecipe_ReturnsRecipe() {
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeMapper.toResponseDTO(testRecipe)).thenReturn(testRecipeResponse);

        RecipeResponseDTO result = recipeService.getRecipeById(1L, USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(USER_ID, result.getUserId());
    }

    @Test
    void getRecipeById_PublicRecipeNotOwned_ReturnsRecipe() {
        Recipe testRecipe = createTestRecipe(OTHER_USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Test Recipe", OTHER_USER_ID, Visibility.PUBLIC);
        
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeMapper.toResponseDTO(testRecipe)).thenReturn(testRecipeResponse);

        RecipeResponseDTO result = recipeService.getRecipeById(1L, USER_ID);

        assertNotNull(result);
        assertEquals(OTHER_USER_ID, result.getUserId());
    }

    @Test
    void getRecipeById_PrivateRecipeNotOwned_ReturnsNull() {
        Recipe testRecipe = createTestRecipe(OTHER_USER_ID, Visibility.PRIVATE);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        RecipeResponseDTO result = recipeService.getRecipeById(1L, USER_ID);

        assertNull(result);
    }

    @Test
    void getRecipeById_RecipeNotFound_ReturnsNull() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        RecipeResponseDTO result = recipeService.getRecipeById(1L, USER_ID);

        assertNull(result);
    }

    @Test
    void createRecipe_Success_ReturnsCreatedRecipe() {
        CreateRecipeRequestDTO testCreateRequest = createTestCreateRequest();
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        
        when(recipeMapper.toEntity(testCreateRequest)).thenReturn(testRecipe);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(recipeMapper.toResponseDTO(testRecipe)).thenReturn(testRecipeResponse);

        RecipeResponseDTO result = recipeService.createRecipe(testCreateRequest, USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(recipeRepository).save(testRecipe);
        assertEquals(USER_ID, testRecipe.getUserId());
    }

    @Test
    void updateRecipe_UserOwnsRecipe_Success() {
        UpdateRecipeRequestDTO testUpdateRequest = createTestUpdateRequest();
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO testRecipeResponse = createTestRecipeResponse(1L, "Updated Recipe", USER_ID, Visibility.PRIVATE);
        
        when(recipeRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(recipeMapper.toResponseDTO(testRecipe)).thenReturn(testRecipeResponse);

        RecipeResponseDTO result = recipeService.updateRecipe(1L, testUpdateRequest, USER_ID);

        assertNotNull(result);
        verify(recipeMapper).updateEntityFromRequest(testUpdateRequest, testRecipe);
        verify(recipeRepository).save(testRecipe);
    }

    @Test
    void updateRecipe_RecipeNotFound_ThrowsException() {
        UpdateRecipeRequestDTO testUpdateRequest = createTestUpdateRequest();
        when(recipeRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recipeService.updateRecipe(1L, testUpdateRequest, USER_ID));
        
        assertEquals("Recipe not found or you don't have permission to update it", exception.getMessage());
    }

    @Test
    void deleteRecipe_UserOwnsRecipe_Success() {
        Recipe testRecipe = createTestRecipe(USER_ID, Visibility.PUBLIC);
        when(recipeRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(testRecipe));

        recipeService.deleteRecipe(1L, USER_ID);

        verify(recipeRepository).delete(testRecipe);
    }

    @Test
    void deleteRecipe_RecipeNotFound_ThrowsException() {
        when(recipeRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recipeService.deleteRecipe(1L, USER_ID));
        
        assertEquals("Recipe not found or you don't have permission to delete it", exception.getMessage());
    }
}