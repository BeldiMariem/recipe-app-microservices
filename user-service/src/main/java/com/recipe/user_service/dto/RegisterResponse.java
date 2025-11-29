package com.recipe.user_service.dto;

import java.time.LocalDateTime;

public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private String message;
    private LocalDateTime registeredAt;

    public RegisterResponse(Long id, String username, String email, String message, LocalDateTime registeredAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.message = message;
        this.registeredAt = registeredAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}