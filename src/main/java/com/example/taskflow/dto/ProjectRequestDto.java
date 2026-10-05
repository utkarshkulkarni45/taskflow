package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRequestDto(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 500) String description,
        @NotNull Long ownerId
) {}