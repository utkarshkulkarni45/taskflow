package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequestDto(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 500) String description,
        String status,
        String priority,
        @NotNull Long projectId
) {}