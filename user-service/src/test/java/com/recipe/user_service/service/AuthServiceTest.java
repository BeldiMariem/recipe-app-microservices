package com.recipe.user_service.service;

import com.recipe.user_service.dto.JwtResponse;
import com.recipe.user_service.dto.LoginRequest;
import com.recipe.user_service.entity.User;
import com.recipe.user_service.repository.UserRepository;
import com.recipe.user_service.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("mariem");
        loginRequest.setPassword("password");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("mariem");
        testUser.setEmail("mariem@gmail.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of("ROLE_USER"));

        authentication = new UsernamePasswordAuthenticationToken(
            "mariem", 
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void authenticateUser_Success_ReturnsJwtResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("mariem")).thenReturn(Optional.of(testUser));

        JwtResponse result = authService.authenticateUser(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("mariem", result.getUsername());
        assertEquals("mariem@gmail.com", result.getEmail());
        assertEquals(1, result.getRoles().size()); 
        assertTrue(result.getRoles().contains("ROLE_USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(authentication);
        verify(userRepository).findByUsername("mariem");
    }

    @Test
    void authenticateUser_AuthenticationFails_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> 
            authService.authenticateUser(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void authenticateUser_UserNotFoundAfterAuth_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("mariem")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            authService.authenticateUser(loginRequest));

        assertEquals("User not found in database after authentication", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(authentication);
        verify(userRepository).findByUsername("mariem");
    }
}