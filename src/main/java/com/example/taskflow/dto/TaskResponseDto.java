// package com.example.taskflow.dto;
package com.example.taskflow.dto;

import java.util.List;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        Long projectId,
        String projectName
) {}