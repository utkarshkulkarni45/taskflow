// package com.example.taskflow.dto;
package com.example.taskflow.dto;

import java.util.List;

public record ProjectResponseDto(
        Long id,
        String title,
        String description,
        List<TaskResponseDto> tasks
) {}