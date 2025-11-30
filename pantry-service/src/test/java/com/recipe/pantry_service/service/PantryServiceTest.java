package com.recipe.pantry_service.service;

import com.recipe.pantry_service.dto.AddPantryItemRequest;
import com.recipe.pantry_service.dto.PantryItemResponse;
import com.recipe.pantry_service.entity.PantryItem;
import com.recipe.pantry_service.mapper.PantryMapper;
import com.recipe.pantry_service.repository.PantryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PantryServiceTest {

    @Mock
    private PantryRepository pantryRepository;

    @Mock
    private PantryMapper pantryMapper;

    @InjectMocks
    private PantryService pantryService;

    private PantryItem testPantryItem;
    private PantryItemResponse testPantryItemResponse;
    private AddPantryItemRequest testAddRequest;
    private final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        testPantryItem = new PantryItem();
        testPantryItem.setId(1L);
        testPantryItem.setUserId(USER_ID);
        testPantryItem.setName("tomato");
        testPantryItem.setQuantity(2.0);
        testPantryItem.setUnit("pieces");
        testPantryItem.setExpiryDate(LocalDate.now().plusDays(5));

        testPantryItemResponse = new PantryItemResponse();
        testPantryItemResponse.setId(1L);
        testPantryItemResponse.setName("tomato");
        testPantryItemResponse.setQuantity(2.0);
        testPantryItemResponse.setUnit("pieces");
        testPantryItemResponse.setExpiryDate(LocalDate.now().plusDays(5));

        testAddRequest = new AddPantryItemRequest();
        testAddRequest.setName("tomato");
        testAddRequest.setQuantity(2.0);
        testAddRequest.setUnit("pieces");
        testAddRequest.setExpiryDate(LocalDate.now().plusDays(5));
    }

    @Test
    void addItem_Success_ReturnsPantryItemResponse() {
        when(pantryMapper.toEntity(testAddRequest, USER_ID)).thenReturn(testPantryItem);
        when(pantryRepository.save(testPantryItem)).thenReturn(testPantryItem);
        when(pantryMapper.toResponse(testPantryItem)).thenReturn(testPantryItemResponse);

        PantryItemResponse result = pantryService.addItem(USER_ID, testAddRequest);

        assertNotNull(result);
        assertEquals("tomato", result.getName());
        assertEquals(2.0, result.getQuantity());
        verify(pantryMapper).toEntity(testAddRequest, USER_ID);
        verify(pantryRepository).save(testPantryItem);
        verify(pantryMapper).toResponse(testPantryItem);
    }

    @Test
    void getUserPantry_Success_ReturnsUserPantryItems() {
        List<PantryItem> pantryItems = List.of(testPantryItem);
        when(pantryRepository.findByUserId(USER_ID)).thenReturn(pantryItems);
        when(pantryMapper.toResponse(testPantryItem)).thenReturn(testPantryItemResponse);

        List<PantryItemResponse> result = pantryService.getUserPantry(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tomato", result.get(0).getName());
        verify(pantryRepository).findByUserId(USER_ID);
        verify(pantryMapper).toResponse(testPantryItem);
    }

    @Test
    void getUserPantry_NoItems_ReturnsEmptyList() {
        when(pantryRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<PantryItemResponse> result = pantryService.getUserPantry(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pantryRepository).findByUserId(USER_ID);
        verify(pantryMapper, never()).toResponse(any());
    }

    @Test
    void getExpiringSoon_Success_ReturnsExpiringItems() {
        List<PantryItem> expiringItems = List.of(testPantryItem);
        when(pantryRepository.findByUserIdAndExpiryDateBefore(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(expiringItems);
        when(pantryMapper.toResponse(testPantryItem)).thenReturn(testPantryItemResponse);

        List<PantryItemResponse> result = pantryService.getExpiringSoon(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tomato", result.get(0).getName());
        verify(pantryRepository).findByUserIdAndExpiryDateBefore(eq(USER_ID), any(LocalDate.class));
        verify(pantryMapper).toResponse(testPantryItem);
    }

    @Test
    void getExpiringSoon_NoExpiringItems_ReturnsEmptyList() {
        when(pantryRepository.findByUserIdAndExpiryDateBefore(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of());

        List<PantryItemResponse> result = pantryService.getExpiringSoon(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pantryRepository).findByUserIdAndExpiryDateBefore(eq(USER_ID), any(LocalDate.class));
        verify(pantryMapper, never()).toResponse(any());
    }

    @Test
    void getItemById_Success_ReturnsPantryItem() {
        when(pantryRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(testPantryItem));
        when(pantryMapper.toResponse(testPantryItem)).thenReturn(testPantryItemResponse);

        PantryItemResponse result = pantryService.getItemById(1L, USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("tomato", result.getName());
        verify(pantryRepository).findByIdAndUserId(1L, USER_ID);
        verify(pantryMapper).toResponse(testPantryItem);
    }

    @Test
    void getItemById_ItemNotFound_ThrowsException() {
        when(pantryRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            pantryService.getItemById(1L, USER_ID));
        
        assertEquals("Pantry item not found", exception.getMessage());
        verify(pantryRepository).findByIdAndUserId(1L, USER_ID);
        verify(pantryMapper, never()).toResponse(any());
    }

    @Test
    void updateItem_Success_ReturnsUpdatedPantryItem() {
        AddPantryItemRequest updateRequest = new AddPantryItemRequest();
        updateRequest.setName("updated tomato");
        updateRequest.setQuantity(3.0);
        updateRequest.setUnit("kg");
        updateRequest.setExpiryDate(LocalDate.now().plusDays(10));

        PantryItemResponse updatedResponse = new PantryItemResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("updated tomato");
        updatedResponse.setQuantity(3.0);
        updatedResponse.setUnit("kg");
        updatedResponse.setExpiryDate(LocalDate.now().plusDays(10));

        when(pantryRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(testPantryItem));
        when(pantryRepository.save(testPantryItem)).thenReturn(testPantryItem);
        when(pantryMapper.toResponse(testPantryItem)).thenReturn(updatedResponse);

        PantryItemResponse result = pantryService.updateItem(1L, updateRequest, USER_ID);

        assertNotNull(result);
        assertEquals("updated tomato", result.getName());
        assertEquals(3.0, result.getQuantity());
        assertEquals("kg", result.getUnit());
        verify(pantryRepository).findByIdAndUserId(1L, USER_ID);
        verify(pantryMapper).updateEntityFromRequest(updateRequest, testPantryItem);
        verify(pantryRepository).save(testPantryItem);
        verify(pantryMapper).toResponse(testPantryItem);
    }

    @Test
    void updateItem_ItemNotFound_ThrowsException() {
        when(pantryRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            pantryService.updateItem(1L, testAddRequest, USER_ID));
        
        assertEquals("Pantry item not found", exception.getMessage());
        verify(pantryRepository).findByIdAndUserId(1L, USER_ID);
        verify(pantryMapper, never()).updateEntityFromRequest(any(), any());
        verify(pantryRepository, never()).save(any());
    }
}