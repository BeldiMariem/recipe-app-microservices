package com.recipe.user_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.recipe.user_service.dto.JwtResponse;
import com.recipe.user_service.dto.LoginRequest;
import com.recipe.user_service.entity.User;
import com.recipe.user_service.repository.UserRepository;
import com.recipe.user_service.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository; 

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtil.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found in database after authentication"));
        
        return new JwtResponse(
            jwt,
            user.getUsername(),
            user.getEmail(),
            user.getRoles()
        );
    }
}