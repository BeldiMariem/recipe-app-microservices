package com.recipe.ai_chef_service.controller;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.ai_chef_service.dto.IngredientDTO;
import com.recipe.ai_chef_service.dto.RecipeGenerationRequest;
import com.recipe.ai_chef_service.dto.RecipeGenerationResponse;
import com.recipe.ai_chef_service.dto.RecipeSuggestion;
import com.recipe.ai_chef_service.service.AIChefService;

@WebFluxTest(AIChefController.class)
class AIChefControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIChefService aiChefService;

    private RecipeGenerationResponse mockResponse;

    @BeforeEach
    void setUp() {
        RecipeSuggestion recipe = new RecipeSuggestion(
                "Test Recipe",
                "Test description for a delicious meal",
                Arrays.asList(
                        new IngredientDTO("Chicken", 200.0, "g"),
                        new IngredientDTO("Rice", 150.0, "g")
                ),
                Arrays.asList("Step 1: Prepare ingredients", "Step 2: Cook everything"),
                30,
                2,
                "easy",
                "Test Cuisine",
                0.9,
                Arrays.asList("salt", "pepper")
        );

        mockResponse = new RecipeGenerationResponse(
                Arrays.asList(recipe),
                "test-generation-id-123",
                System.currentTimeMillis()
        );
    }

    @Test
    void generateRecipes_ShouldReturnRecipes_WhenValidRequest() {
        RecipeGenerationRequest request = new RecipeGenerationRequest();
        request.setMealType("dinner");
        request.setMaxPreparationTime(30);
        request.setServings(2);
        request.setDifficulty("easy");

        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenReturn(mockResponse);

        webTestClient.post()
                .uri("/api/ai/generate-recipes")
                .header("User-Id", "test-user-123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.suggestions.length()").isEqualTo(1)
                .jsonPath("$.suggestions[0].title").isEqualTo("Test Recipe")
                .jsonPath("$.suggestions[0].description").isEqualTo("Test description for a delicious meal")
                .jsonPath("$.suggestions[0].preparationTime").isEqualTo(30)
                .jsonPath("$.suggestions[0].servings").isEqualTo(2)
                .jsonPath("$.suggestions[0].difficulty").isEqualTo("easy")
                .jsonPath("$.suggestions[0].cuisine").isEqualTo("Test Cuisine")
                .jsonPath("$.suggestions[0].confidenceScore").isEqualTo(0.9)
                .jsonPath("$.suggestions[0].missingIngredients.length()").isEqualTo(2)
                .jsonPath("$.generationId").isEqualTo("test-generation-id-123")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void generateRecipes_ShouldReturnInternalError_WhenServiceThrowsException() {
        RecipeGenerationRequest request = new RecipeGenerationRequest();
        request.setMealType("dinner");

        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        webTestClient.post()
                .uri("/api/ai/generate-recipes")
                .header("User-Id", "test-user-123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getQuickSuggestions_ShouldReturnRecipes_WithQueryParams() {
        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenReturn(mockResponse);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ai/quick-suggestions")
                        .queryParam("mealType", "dinner")
                        .queryParam("maxTime", "30")
                        .build())
                .header("User-Id", "test-user-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.suggestions.length()").isEqualTo(1)
                .jsonPath("$.suggestions[0].title").isEqualTo("Test Recipe");
    }

    @Test
    void getQuickSuggestions_ShouldReturnRecipes_WithoutQueryParams() {
        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenReturn(mockResponse);

        webTestClient.get()
                .uri("/api/ai/quick-suggestions")
                .header("User-Id", "test-user-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.suggestions.length()").isEqualTo(1)
                .jsonPath("$.suggestions[0].title").isEqualTo("Test Recipe");
    }

    @Test
    void getQuickSuggestions_ShouldReturnInternalError_WhenServiceFails() {
        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        webTestClient.get()
                .uri("/api/ai/quick-suggestions?mealType=dinner")
                .header("User-Id", "test-user-123")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getUseItUpRecipes_ShouldReturnRecipes() {
        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenReturn(mockResponse);

        webTestClient.get()
                .uri("/api/ai/use-it-up")
                .header("User-Id", "test-user-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.suggestions.length()").isEqualTo(1)
                .jsonPath("$.suggestions[0].title").isEqualTo("Test Recipe");
    }

    @Test
    void getUseItUpRecipes_ShouldReturnInternalError_WhenServiceFails() {
        when(aiChefService.generateRecipes(any(RecipeGenerationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        webTestClient.get()
                .uri("/api/ai/use-it-up")
                .header("User-Id", "test-user-123")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getUseItUpRecipes_ShouldReturnBadRequest_WhenMissingUserId() {
        webTestClient.get()
                .uri("/api/ai/use-it-up")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void generateRecipes_ShouldReturnBadRequest_WhenMissingUserIdHeader() {
        RecipeGenerationRequest request = new RecipeGenerationRequest();
        request.setMealType("dinner");

        webTestClient.post()
                .uri("/api/ai/generate-recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}