package com.recipe.pantry_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.dto.PantryItemResponse;
import com.recipe.pantry_service.service.PantryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PantryController.class)
class PantryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PantryService pantryService;

    private final String USER_ID = "user123";
    private final String USER_HEADER = "User-Id";

    private PantryItemResponse createPantryItemResponse(Long id, String name, Double quantity, String unit, LocalDate expiryDate) {
        PantryItemResponse response = new PantryItemResponse();
        response.setId(id);
        response.setName(name);
        response.setQuantity(quantity);
        response.setUnit(unit);
        response.setExpiryDate(expiryDate);
        return response;
    }

    private AddPantryItemRequest createAddPantryItemRequest(String name, Double quantity, String unit, LocalDate expiryDate) {
        AddPantryItemRequest request = new AddPantryItemRequest();
        request.setName(name);
        request.setQuantity(quantity);
        request.setUnit(unit);
        request.setExpiryDate(expiryDate);
        return request;
    }

    @Test
    void addItem_Success() throws Exception {
        AddPantryItemRequest request = createAddPantryItemRequest("tomato", 2.0, "pieces", LocalDate.now().plusDays(5));
        PantryItemResponse response = createPantryItemResponse(1L, "tomato", 2.0, "pieces", LocalDate.now().plusDays(5));
        
        when(pantryService.addItem(eq(USER_ID), any(AddPantryItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/pantry/items/addItem")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("tomato")))
                .andExpect(jsonPath("$.quantity", is(2.0)))
                .andExpect(jsonPath("$.unit", is("pieces")));
    }

    @Test
    void addItem_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        AddPantryItemRequest request = createAddPantryItemRequest("tomato", 2.0, "pieces", LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/pantry/items/addItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_InvalidJSON_ReturnsBadRequest() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/pantry/items/addItem")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserPantry_Success() throws Exception {
        PantryItemResponse response1 = createPantryItemResponse(1L, "tomato", 2.0, "pieces", LocalDate.now().plusDays(5));
        PantryItemResponse response2 = createPantryItemResponse(2L, "milk", 1.0, "liter", LocalDate.now().plusDays(2));
        List<PantryItemResponse> responses = List.of(response1, response2);
        
        when(pantryService.getUserPantry(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/pantry/items")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("tomato")))
                .andExpect(jsonPath("$[1].name", is("milk")));
    }

    @Test
    void getUserPantry_EmptyPantry_ReturnsEmptyArray() throws Exception {
        when(pantryService.getUserPantry(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/pantry/items")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUserPantry_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pantry/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getExpiringItems_Success() throws Exception {
        PantryItemResponse response = createPantryItemResponse(1L, "milk", 1.0, "liter", LocalDate.now().plusDays(1));
        List<PantryItemResponse> responses = List.of(response);
        
        when(pantryService.getExpiringSoon(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/pantry/items/expiring")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("milk")))
                .andExpect(jsonPath("$[0].quantity", is(1.0)));
    }

    @Test
    void getExpiringItems_NoExpiringItems_ReturnsEmptyArray() throws Exception {
        when(pantryService.getExpiringSoon(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/pantry/items/expiring")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getExpiringItems_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pantry/items/expiring"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_Success() throws Exception {
        PantryItemResponse response = createPantryItemResponse(1L, "tomato", 2.0, "pieces", LocalDate.now().plusDays(5));
        when(pantryService.getItemById(1L, USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/pantry/items/getItemById/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("tomato")))
                .andExpect(jsonPath("$.quantity", is(2.0)))
                .andExpect(jsonPath("$.unit", is("pieces")));
    }

    @Test
    void getItemById_ItemNotFound_ReturnsNotFound() throws Exception {
        when(pantryService.getItemById(1L, USER_ID))
                .thenThrow(new RuntimeException("Pantry item not found"));

        mockMvc.perform(get("/api/pantry/items/getItemById/1")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound()); 
    }

    @Test
    void getItemById_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pantry/items/getItemById/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_Success() throws Exception {
        AddPantryItemRequest request = createAddPantryItemRequest("updated tomato", 3.0, "kg", LocalDate.now().plusDays(10));
        PantryItemResponse response = createPantryItemResponse(1L, "updated tomato", 3.0, "kg", LocalDate.now().plusDays(10));
        
        when(pantryService.updateItem(eq(1L), any(AddPantryItemRequest.class), eq(USER_ID))).thenReturn(response);

        mockMvc.perform(put("/api/pantry/items/updateItem/1")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("updated tomato")))
                .andExpect(jsonPath("$.quantity", is(3.0)))
                .andExpect(jsonPath("$.unit", is("kg")));
    }

    @Test
    void updateItem_ItemNotFound_ReturnsNotFound() throws Exception {
        AddPantryItemRequest request = createAddPantryItemRequest("tomato", 2.0, "pieces", LocalDate.now().plusDays(5));
        
        when(pantryService.updateItem(eq(1L), any(AddPantryItemRequest.class), eq(USER_ID)))
                .thenThrow(new RuntimeException("Pantry item not found"));

        mockMvc.perform(put("/api/pantry/items/updateItem/1")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); 
    }

    @Test
    void updateItem_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        AddPantryItemRequest request = createAddPantryItemRequest("tomato", 2.0, "pieces", LocalDate.now().plusDays(5));

        mockMvc.perform(put("/api/pantry/items/updateItem/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_InvalidJSON_ReturnsBadRequest() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(put("/api/pantry/items/updateItem/1")
                .header(USER_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidEndpoint_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/pantry/invalid-endpoint")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongHttpMethod_ReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/pantry/items")
                .header(USER_HEADER, USER_ID))
                .andExpect(status().isMethodNotAllowed());
    }
}

