package com.recipe.user_service.mapper;

import org.springframework.stereotype.Component;

import com.recipe.user_service.dto.RegisterRequest;
import com.recipe.user_service.dto.RegisterResponse;
import com.recipe.user_service.entity.User;

@Component 
public class UserMapper {

    public User toEntity(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); 
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        return user;
    }

    public RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            "User registered successfully",
            user.getCreatedAt()
        );
    }
}