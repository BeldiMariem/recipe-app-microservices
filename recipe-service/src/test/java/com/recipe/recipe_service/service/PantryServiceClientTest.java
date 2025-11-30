package com.recipe.recipe_service.service;

import com.recipe.recipe_service.dto.PantryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryServiceClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PantryServiceClient pantryServiceClient;

    private final String USER_ID = "user123";
    private PantryItem testPantryItem;

    @BeforeEach
    void setUp() {
        testPantryItem = new PantryItem();
        testPantryItem.setId(1L);
        testPantryItem.setName("tomato");
        testPantryItem.setQuantity(2.0);
        testPantryItem.setUnit("pieces");
        testPantryItem.setUserId(USER_ID);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getUserPantry_Success_ReturnsPantryItems() {
        List<PantryItem> expectedItems = List.of(testPantryItem);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(expectedItems));

        List<PantryItem> result = pantryServiceClient.getUserPantry(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tomato", result.get(0).getName());
        
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("http://pantry-service/api/pantry/items/getItems");
        verify(requestHeadersSpec).header("User-Id", USER_ID);
        verify(requestHeadersSpec).retrieve();
    }

    @Test
    void getUserPantry_ServiceUnavailable_ReturnsEmptyList() {
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        List<PantryItem> result = pantryServiceClient.getUserPantry(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("http://pantry-service/api/pantry/items/getItems");
    }

    @Test
    void getUserPantry_WebClientError_ReturnsEmptyList() {
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(WebClientResponseException.create(503, "Service Unavailable", null, null, null)));

        List<PantryItem> result = pantryServiceClient.getUserPantry(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserPantry_NullResponse_ReturnsEmptyList() {
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of()));

        List<PantryItem> result = pantryServiceClient.getUserPantry(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExpiringItems_Success_ReturnsExpiringItems() {
        List<PantryItem> expectedItems = List.of(testPantryItem);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(expectedItems));

        List<PantryItem> result = pantryServiceClient.getExpiringItems(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tomato", result.get(0).getName());
        
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("http://pantry-service/api/pantry/items/expiring");
        verify(requestHeadersSpec).header("User-Id", USER_ID);
        verify(requestHeadersSpec).retrieve();
    }

    @Test
    void getExpiringItems_ServiceError_ReturnsEmptyList() {
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        List<PantryItem> result = pantryServiceClient.getExpiringItems(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExpiringItems_EmptyResponse_ReturnsEmptyList() {
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of()));

        List<PantryItem> result = pantryServiceClient.getExpiringItems(USER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExpiringItems_NullUserId_HandlesGracefully() {
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of()));

        List<PantryItem> result = pantryServiceClient.getExpiringItems(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(requestHeadersSpec).header(eq("User-Id"), isNull());
    }
}