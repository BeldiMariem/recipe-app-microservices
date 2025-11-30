package com.recipe.user_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.user_service.dto.RegisterRequest;
import com.recipe.user_service.dto.RegisterResponse;
import com.recipe.user_service.entity.User;
import com.recipe.user_service.repository.UserRepository;

@Service
@Transactional
public class UserService {

  @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public RegisterResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        user.getRoles().add("ROLE_USER");

        User savedUser = userRepository.save(user);
        
        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            "User registered successfully",
            savedUser.getCreatedAt()
        );
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}