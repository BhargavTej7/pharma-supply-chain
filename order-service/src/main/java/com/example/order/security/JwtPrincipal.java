package com.example.order.security;

public record JwtPrincipal(String email, Long userId, String role) {}
