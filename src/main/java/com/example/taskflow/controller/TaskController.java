package com.example.taskflow.controller;

import com.example.taskflow.dto.TaskRequestDto;
import com.example.taskflow.dto.TaskResponseDto;
import com.example.taskflow.mapper.EntityMapper;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.Task;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          EntityMapper entityMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.entityMapper = entityMapper;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Unauthorized");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @GetMapping
    public List<TaskResponseDto> getAllTasks(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return taskRepository.findAll().stream()
                .filter(t -> t.getProject() != null && t.getProject().getOwner() != null
                        && t.getProject().getOwner().getId().equals(user.getId()))
                .map(entityMapper::toTaskDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.getOwner() == null || !project.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
        }

        List<TaskResponseDto> dtos = taskRepository.findByProjectId(projectId).stream()
                .map(entityMapper::toTaskDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return taskRepository.findById(id)
                .filter(t -> t.getProject() != null && t.getProject().getOwner() != null
                        && t.getProject().getOwner().getId().equals(user.getId()))
                .map(entityMapper::toTaskDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @CacheEvict(value = "projects", key = "#authentication.name")
    public ResponseEntity<?> createTask(@RequestBody @Valid TaskRequestDto requestDto, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Project project = projectRepository.findById(requestDto.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getOwner() == null || !project.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied: Project does not belong to user"));
        }

        Task task = entityMapper.toTaskEntity(requestDto, project);
        Task savedTask = taskRepository.save(task);

        return ResponseEntity.ok(entityMapper.toTaskDto(savedTask));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "projects", key = "#authentication.name")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                        @RequestBody @Valid TaskRequestDto requestDto,
                                        Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Task existingTask = taskRepository.findById(id).orElse(null);

        if (existingTask == null || existingTask.getProject() == null
                || existingTask.getProject().getOwner() == null
                || !existingTask.getProject().getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Task not found or access denied"));
        }

        existingTask.setTitle(requestDto.title());
        existingTask.setDescription(requestDto.description());
        if (requestDto.status() != null && !requestDto.status().isBlank()) {
            existingTask.setStatus(requestDto.status().toUpperCase());
        }
        if (requestDto.priority() != null && !requestDto.priority().isBlank()) {
            existingTask.setPriority(requestDto.priority().toUpperCase());
        }

        Task updatedTask = taskRepository.save(existingTask);
        return ResponseEntity.ok(entityMapper.toTaskDto(updatedTask));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "projects", key = "#authentication.name")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Task existingTask = taskRepository.findById(id).orElse(null);

        if (existingTask == null || existingTask.getProject() == null
                || existingTask.getProject().getOwner() == null
                || !existingTask.getProject().getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Task not found or access denied"));
        }

        taskRepository.delete(existingTask);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }
}