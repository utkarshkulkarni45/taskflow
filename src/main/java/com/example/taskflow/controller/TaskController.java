package com.example.taskflow.controller;

import com.example.taskflow.dto.TaskRequestDto;
import com.example.taskflow.dto.TaskResponseDto;
import com.example.taskflow.mapper.EntityMapper;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.Task;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EntityMapper entityMapper;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          EntityMapper entityMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.entityMapper = entityMapper;
    }

    @GetMapping
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(entityMapper::toTaskDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(entityMapper::toTaskDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody @Valid TaskRequestDto requestDto) {
        Project project = projectRepository.findById(requestDto.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = entityMapper.toTaskEntity(requestDto, project);
        Task savedTask = taskRepository.save(task);

        return ResponseEntity.ok(entityMapper.toTaskDto(savedTask));
    }
}