package com.recipe.user_service.service;

import com.recipe.user_service.dto.RegisterRequest;
import com.recipe.user_service.dto.RegisterResponse;
import com.recipe.user_service.entity.User;
import com.recipe.user_service.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("mariem");
        registerRequest.setEmail("mariem@gmail.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("Mariem");
        registerRequest.setLastName("Beldi");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("mariem");
        testUser.setEmail("mariem@gmail.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Mariem");
        testUser.setLastName("Beldi");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    void registerUser_Success_ReturnsRegisterResponse() {
        when(userRepository.existsByUsername("mariem")).thenReturn(false);
        when(userRepository.existsByEmail("mariem@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterResponse result = userService.registerUser(registerRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("mariem", result.getUsername());
        assertEquals("mariem@gmail.com", result.getEmail());
        assertEquals("User registered successfully", result.getMessage());

        verify(userRepository).existsByUsername("mariem");
        verify(userRepository).existsByEmail("mariem@gmail.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameTaken_ThrowsException() {
        when(userRepository.existsByUsername("mariem")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.registerUser(registerRequest));

        assertEquals("Username is already taken", exception.getMessage());

        verify(userRepository).existsByUsername("mariem");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailInUse_ThrowsException() {
        when(userRepository.existsByUsername("mariem")).thenReturn(false);
        when(userRepository.existsByEmail("mariem@gmail.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.registerUser(registerRequest));

        assertEquals("Email is already in use", exception.getMessage());

        verify(userRepository).existsByUsername("mariem");
        verify(userRepository).existsByEmail("mariem@gmail.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_Success_ReturnsUser() {
        when(userRepository.findByUsername("mariem")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("mariem");

        assertTrue(result.isPresent());
        assertEquals("mariem", result.get().getUsername());
        assertEquals("Mariem", result.get().getFirstName());
        assertEquals("Beldi", result.get().getLastName());
        verify(userRepository).findByUsername("mariem");
    }

    @Test
    void findByUsername_NotFound_ReturnsEmpty() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("unknown");

        assertTrue(result.isEmpty());
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void findById_Success_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("mariem", result.get().getUsername());
        assertEquals("Mariem", result.get().getFirstName());
        assertEquals("Beldi", result.get().getLastName());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_NotFound_ReturnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(999L);
    }
}