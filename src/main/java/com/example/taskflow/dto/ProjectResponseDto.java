package com.example.taskflow.dto;

import java.io.Serializable;
import java.util.List;

public record ProjectResponseDto(
        Long id,
        String title,
        String description,
        List<TaskResponseDto> tasks
) implements Serializable {}