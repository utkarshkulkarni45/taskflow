package com.example.taskflow.dto;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String email
) {}

