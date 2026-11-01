package com.example.taskflow.dto;

import java.io.Serializable;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        Long projectId,
        String projectName
) implements Serializable {}