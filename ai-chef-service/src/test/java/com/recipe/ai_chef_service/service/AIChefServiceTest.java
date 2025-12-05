package com.recipe.ai_chef_service.service;

import com.recipe.ai_chef_service.client.PantryServiceClient;
import com.recipe.ai_chef_service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIChefServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private PantryServiceClient pantryServiceClient;

    @InjectMocks
    private AIChefService aiChefService;

    private String userId;
    private RecipeGenerationRequest request;
    private List<PantryItemDTO> pantryItems;

    @BeforeEach
    void setUp() {
        userId = "user123";
        request = new RecipeGenerationRequest();
        request.setUserId(userId);
        request.setMealType("dinner");
        request.setServings(2);
        request.setDifficulty("easy");

        pantryItems = Arrays.asList(
            new PantryItemDTO(1L, userId, "chicken breast", 500.0, "g", 
                LocalDate.now().plusDays(5), LocalDate.now(), false),
            new PantryItemDTO(2L, userId, "carrot", 300.0, "g", 
                LocalDate.now().plusDays(3), LocalDate.now(), false),
            new PantryItemDTO(3L, userId, "rice", 1000.0, "g", 
                LocalDate.now().plusMonths(1), LocalDate.now(), false)
        );

        ReflectionTestUtils.setField(aiChefService, "useExpiringFirst", true);
    }

    @Test
    void generateRecipes_SuccessWithGemini() {
        List<PantryItemDTO> expiringItems = Collections.emptyList();
        when(pantryServiceClient.getUserPantry(userId)).thenReturn(pantryItems);
        when(pantryServiceClient.getExpiringItems(userId)).thenReturn(expiringItems);
        
        RecipeSuggestion mockSuggestion = createMockRecipeSuggestion();
        
        when(geminiService.generateRecipesWithGemini(anyList(), any(RecipeGenerationRequest.class)))
            .thenReturn(Arrays.asList(mockSuggestion));

        RecipeGenerationResponse response = aiChefService.generateRecipes(request);

        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("Chicken Fried Rice", response.getSuggestions().get(0).getTitle());
        assertNotNull(response.getGenerationId());
        assertNotNull(response.getTimestamp());
        
        verify(pantryServiceClient).getUserPantry(userId);
        verify(pantryServiceClient).getExpiringItems(userId);
        verify(geminiService).generateRecipesWithGemini(anyList(), any(RecipeGenerationRequest.class));
    }

    @Test
    void generateRecipes_EmptyPantry() {
        when(pantryServiceClient.getUserPantry(userId)).thenReturn(Collections.emptyList());

        RecipeGenerationResponse response = aiChefService.generateRecipes(request);

        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        RecipeSuggestion suggestion = response.getSuggestions().get(0);
        assertEquals("Pantry is Empty", suggestion.getTitle());
        assertEquals(0, suggestion.getPreparationTime());
        assertTrue(suggestion.getDescription().contains("Add some ingredients"));
        assertTrue(response.getGenerationId().startsWith("empty-pantry-"));
        
        verify(pantryServiceClient).getUserPantry(userId);
        verify(pantryServiceClient, never()).getExpiringItems(userId);
        verify(geminiService, never()).generateRecipesWithGemini(anyList(), any());
    }
    @Test
    void generateRecipes_WithExpiringItems_Prioritized() {
        List<PantryItemDTO> expiringItems = Arrays.asList(
            new PantryItemDTO(2L, userId, "carrot", 300.0, "g", 
                LocalDate.now().plusDays(1), LocalDate.now(), false)
        );
        
        when(pantryServiceClient.getUserPantry(userId)).thenReturn(pantryItems);
        when(pantryServiceClient.getExpiringItems(userId)).thenReturn(expiringItems);
        when(geminiService.generateRecipesWithGemini(anyList(), any(RecipeGenerationRequest.class)))
            .thenReturn(Collections.singletonList(createMockRecipeSuggestion()));

        RecipeGenerationResponse response = aiChefService.generateRecipes(request);

        assertNotNull(response);
        verify(pantryServiceClient).getExpiringItems(userId);
    }

    @Test
    void generateRecipes_UseExpiringFirstDisabled() {
        ReflectionTestUtils.setField(aiChefService, "useExpiringFirst", false);
        
        when(pantryServiceClient.getUserPantry(userId)).thenReturn(pantryItems);
        when(geminiService.generateRecipesWithGemini(anyList(), any(RecipeGenerationRequest.class)))
            .thenReturn(Collections.singletonList(createMockRecipeSuggestion()));

        RecipeGenerationResponse response = aiChefService.generateRecipes(request);

        assertNotNull(response);
        verify(pantryServiceClient, never()).getExpiringItems(userId);
    }

    @Test
    void generateRecipes_NullServings_DefaultsToZero() {
        request.setServings(null);
        when(pantryServiceClient.getUserPantry(userId)).thenReturn(pantryItems);
        when(pantryServiceClient.getExpiringItems(userId)).thenReturn(Collections.emptyList());
        when(geminiService.generateRecipesWithGemini(anyList(), any(RecipeGenerationRequest.class)))
            .thenReturn(Collections.singletonList(createMockRecipeSuggestion()));

        RecipeGenerationResponse response = aiChefService.generateRecipes(request);

        assertNotNull(response);
        verify(geminiService).generateRecipesWithGemini(anyList(), any(RecipeGenerationRequest.class));
    }

    @Test
    void prioritizeExpiringItems_MixedItems() {
        List<PantryItemDTO> allItems = Arrays.asList(
            new PantryItemDTO("chicken", 500.0, "g", LocalDate.now().plusDays(10)),
            new PantryItemDTO("tomato", 300.0, "g", LocalDate.now().plusDays(2)),
            new PantryItemDTO("onion", 200.0, "g", LocalDate.now().plusDays(1))
        );

        List<PantryItemDTO> expiringItems = Arrays.asList(
            new PantryItemDTO("onion", 200.0, "g", LocalDate.now().plusDays(1)),
            new PantryItemDTO("tomato", 300.0, "g", LocalDate.now().plusDays(2))
        );

        List<PantryItemDTO> result = (List<PantryItemDTO>) ReflectionTestUtils.invokeMethod(
            aiChefService, "prioritizeExpiringItems", allItems, expiringItems);

        assertEquals(3, result.size());
        assertTrue(result.get(0).getName().equals("onion") || result.get(0).getName().equals("tomato"));
        assertTrue(result.get(1).getName().equals("onion") || result.get(1).getName().equals("tomato"));
        assertEquals("chicken", result.get(2).getName());
    }

    @Test
    void categorizeIngredient_VariousIngredients() {
        assertEquals("protein", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "chicken breast"));
        assertEquals("protein", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "beef steak"));
        assertEquals("protein", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "tofu"));

        assertEquals("grain", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "rice"));
        assertEquals("grain", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "pasta"));

        assertEquals("vegetable", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "tomato"));
        assertEquals("vegetable", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "broccoli"));

        assertEquals("dairy", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "milk"));
        assertEquals("dairy", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "cheese"));

        assertEquals("condiment", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "olive oil"));

        assertEquals("other", ReflectionTestUtils.invokeMethod(
            aiChefService, "categorizeIngredient", "unknown ingredient"));
    }

    private RecipeSuggestion createMockRecipeSuggestion() {
        return new RecipeSuggestion(
            "Chicken Fried Rice",
            "Delicious fried rice with chicken",
            Arrays.asList(
                new IngredientDTO("chicken breast", 200.0, "g"),
                new IngredientDTO("rice", 150.0, "g")
            ),
            Arrays.asList("Step 1", "Step 2"),
            20,
            2,
            "easy",
            "Asian",
            0.9,
            Arrays.asList("soy sauce", "oil")
        );
    }
}