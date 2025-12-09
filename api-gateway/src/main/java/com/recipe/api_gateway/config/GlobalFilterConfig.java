package com.recipe.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalFilterConfig implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GlobalFilterConfig.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        logger.info("Incoming request: {} {}", request.getMethod(), request.getURI());
        
        if (requiresAuthentication(request)) {
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private boolean requiresAuthentication(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return !path.startsWith("/api/recipes/public")&&
               !path.startsWith("/api/recipes/getRecipeById/{id}")&&
               !path.startsWith("/auth/login") && 
               !path.startsWith("/auth/register") &&
               !path.contains("/actuator/health");
    }
}