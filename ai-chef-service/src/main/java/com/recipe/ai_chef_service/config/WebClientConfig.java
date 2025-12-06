package com.recipe.ai_chef_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Value("${pantry.service.url}")
    private String pantryServiceUrl;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    @Value("${recipe.service.url}")
    private String recipeServiceUrl;
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    
    @Bean
    public WebClient pantryServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(pantryServiceUrl).build();
    }
    
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(userServiceUrl).build();
    }
    
    @Bean
    public WebClient recipeServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(recipeServiceUrl).build();
    }
}