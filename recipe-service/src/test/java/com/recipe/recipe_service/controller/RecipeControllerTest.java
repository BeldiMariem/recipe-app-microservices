package com.recipe.recipe_service.controller;

import com.recipe.recipe_service.dto.CreateRecipeRequestDTO;
import com.recipe.recipe_service.dto.RecipeResponseDTO;
import com.recipe.recipe_service.dto.UpdateRecipeRequestDTO;
import com.recipe.recipe_service.entity.Visibility;
import com.recipe.recipe_service.service.RecipeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {
    private MockMvc mockMvc;
    
    @Mock
    private RecipeService recipeService;
    
    @InjectMocks
    private RecipeController recipeController;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController).build();
    }

    private final String USER_ID = "user123";
    private final String USER_HEADER = "User-Id";

    private RecipeResponseDTO createRecipeResponse(Long id, String title, String userId, Visibility visibility) {
        RecipeResponseDTO response = new RecipeResponseDTO();
        response.setId(id);
        response.setTitle(title);
        response.setUserId(userId);
        response.setVisibility(visibility);
        response.setDescription("Test description");
        response.setPreparationTime(30);
        response.setServings(4);
        response.setDifficulty("EASY");
        response.setRating(4.5);
        response.setRatingCount(10);
        return response;
    }

    private CreateRecipeRequestDTO createCreateRecipeRequest() {
        CreateRecipeRequestDTO request = new CreateRecipeRequestDTO();
        request.setTitle("Test Recipe");
        request.setDescription("Test Description");
        request.setPreparationTime(30);
        request.setServings(4);
        request.setDifficulty("EASY");
        request.setVisibility(Visibility.PUBLIC);
        return request;
    }

    private UpdateRecipeRequestDTO createUpdateRecipeRequest() {
        UpdateRecipeRequestDTO request = new UpdateRecipeRequestDTO();
        request.setTitle("Updated Recipe");
        request.setDescription("Updated Description");
        request.setPreparationTime(45);
        request.setServings(6);
        request.setDifficulty("MEDIUM");
        request.setVisibility(Visibility.PRIVATE);
        return request;
    }

    @Test
    void getRecipeSuggestions_Success() throws Exception {
        RecipeResponseDTO response1 = createRecipeResponse(1L, "Pasta", USER_ID, Visibility.PUBLIC);
        RecipeResponseDTO response2 = createRecipeResponse(2L, "Salad", USER_ID, Visibility.PUBLIC);
        List<RecipeResponseDTO> responses = Arrays.asList(response1, response2);
        
        when(recipeService.getRecipeSuggestions(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/recipes/suggestions")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Pasta")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Salad")));
    }

    @Test
    void getRecipeSuggestions_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes/suggestions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecipeSuggestions_EmptyList_ReturnsEmptyArray() throws Exception {
        when(recipeService.getRecipeSuggestions(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/recipes/suggestions")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUseItUpRecipes_Success() throws Exception {
        RecipeResponseDTO response = createRecipeResponse(1L, "Use It Up Recipe", USER_ID, Visibility.PUBLIC);
        List<RecipeResponseDTO> responses = List.of(response);
        
        when(recipeService.getUseItUpRecipes(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/recipes/use-it-up")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Use It Up Recipe")));
    }

    @Test
    void getUseItUpRecipes_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes/use-it-up"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRecipes_Success() throws Exception {
        RecipeResponseDTO response1 = createRecipeResponse(1L, "Public Recipe", "user1", Visibility.PUBLIC);
        RecipeResponseDTO response2 = createRecipeResponse(2L, "My Private Recipe", USER_ID, Visibility.PRIVATE);
        List<RecipeResponseDTO> responses = Arrays.asList(response1, response2);
        
        when(recipeService.getAllRecipes(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/recipes/all")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Public Recipe")))
                .andExpect(jsonPath("$[1].title", is("My Private Recipe")));
    }

    @Test
    void getAllRecipes_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPublicRecipes_Success() throws Exception {
        RecipeResponseDTO response1 = createRecipeResponse(1L, "Public Recipe 1", "user1", Visibility.PUBLIC);
        RecipeResponseDTO response2 = createRecipeResponse(2L, "Public Recipe 2", "user2", Visibility.PUBLIC);
        List<RecipeResponseDTO> responses = Arrays.asList(response1, response2);
        
        when(recipeService.getPublicRecipes()).thenReturn(responses);

        mockMvc.perform(get("/api/recipes/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Public Recipe 1")))
                .andExpect(jsonPath("$[1].title", is("Public Recipe 2")));
    }

    @Test
    void getPublicRecipes_NoPublicRecipes_ReturnsEmptyArray() throws Exception {
        when(recipeService.getPublicRecipes()).thenReturn(List.of());

        mockMvc.perform(get("/api/recipes/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMyRecipes_Success() throws Exception {
        RecipeResponseDTO response1 = createRecipeResponse(1L, "My Recipe 1", USER_ID, Visibility.PRIVATE);
        RecipeResponseDTO response2 = createRecipeResponse(2L, "My Recipe 2", USER_ID, Visibility.PUBLIC);
        List<RecipeResponseDTO> responses = Arrays.asList(response1, response2);
        
        when(recipeService.getMyRecipes(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/recipes/my-recipes")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("My Recipe 1")))
                .andExpect(jsonPath("$[1].title", is("My Recipe 2")));
    }

    @Test
    void getMyRecipes_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes/my-recipes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyRecipes_NoRecipes_ReturnsEmptyArray() throws Exception {
        when(recipeService.getMyRecipes(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/recipes/my-recipes")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getRecipeById_Success() throws Exception {
        RecipeResponseDTO response = createRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        when(recipeService.getRecipeById(1L, USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/recipes/getRecipeById/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Recipe")))
                .andExpect(jsonPath("$.userId", is(USER_ID)))
                .andExpect(jsonPath("$.visibility", is("PUBLIC")));
    }

    @Test
    void getRecipeById_NotFound() throws Exception {
        when(recipeService.getRecipeById(1L, USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/recipes/getRecipeById/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRecipeById_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes/getRecipeById/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecipeById_InvalidId_ReturnsNotFound() throws Exception {
        when(recipeService.getRecipeById(999L, USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/recipes/getRecipeById/999")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRecipe_Success() throws Exception {
        CreateRecipeRequestDTO request = createCreateRecipeRequest();
        RecipeResponseDTO response = createRecipeResponse(1L, "Test Recipe", USER_ID, Visibility.PUBLIC);
        
        when(recipeService.createRecipe(any(CreateRecipeRequestDTO.class), eq(USER_ID))).thenReturn(response);

        mockMvc.perform(post("/api/recipes/createRecipe")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Recipe")))
                .andExpect(jsonPath("$.userId", is(USER_ID)));
    }

    @Test
    void createRecipe_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        CreateRecipeRequestDTO request = createCreateRecipeRequest();

        mockMvc.perform(post("/api/recipes/createRecipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void createRecipe_InvalidJSON_ReturnsBadRequest() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/recipes/createRecipe")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRecipe_Success() throws Exception {
        UpdateRecipeRequestDTO request = createUpdateRecipeRequest();
        RecipeResponseDTO response = createRecipeResponse(1L, "Updated Recipe", USER_ID, Visibility.PRIVATE);
        
        when(recipeService.updateRecipe(eq(1L), any(UpdateRecipeRequestDTO.class), eq(USER_ID))).thenReturn(response);

        mockMvc.perform(put("/api/recipes/updateRecipe/1")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Recipe")))
                .andExpect(jsonPath("$.visibility", is("PRIVATE")));
    }

    @Test
    void updateRecipe_NotFound() throws Exception {
        UpdateRecipeRequestDTO request = createUpdateRecipeRequest();
        
        when(recipeService.updateRecipe(eq(1L), any(UpdateRecipeRequestDTO.class), eq(USER_ID)))
                .thenThrow(new RuntimeException("Recipe not found"));

        mockMvc.perform(put("/api/recipes/updateRecipe/1")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRecipe_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        UpdateRecipeRequestDTO request = createUpdateRecipeRequest();

        mockMvc.perform(put("/api/recipes/updateRecipe/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRecipe_InvalidId_ReturnsNotFound() throws Exception {
        UpdateRecipeRequestDTO request = createUpdateRecipeRequest();
        
        when(recipeService.updateRecipe(eq(999L), any(UpdateRecipeRequestDTO.class), eq(USER_ID)))
                .thenThrow(new RuntimeException("Recipe not found"));

        mockMvc.perform(put("/api/recipes/updateRecipe/999")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRecipe_Success() throws Exception {
        doNothing().when(recipeService).deleteRecipe(1L, USER_ID);

        mockMvc.perform(delete("/api/recipes/deleteRecipe/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNoContent());

        verify(recipeService).deleteRecipe(1L, USER_ID);
    }

    @Test
    void deleteRecipe_NotFound() throws Exception {
        doThrow(new RuntimeException("Recipe not found")).when(recipeService).deleteRecipe(1L, USER_ID);

        mockMvc.perform(delete("/api/recipes/deleteRecipe/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());

        verify(recipeService).deleteRecipe(1L, USER_ID);
    }

    @Test
    void deleteRecipe_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/recipes/deleteRecipe/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRecipe_InvalidId_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("Recipe not found")).when(recipeService).deleteRecipe(999L, USER_ID);

        mockMvc.perform(delete("/api/recipes/deleteRecipe/999")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidEndpoint_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/recipes/invalid-endpoint")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongHttpMethod_ReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/recipes/getRecipeById/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isMethodNotAllowed());
    }
}