package com.example.taskflow.controller;

import com.example.taskflow.dto.ProjectRequestDto;
import com.example.taskflow.dto.ProjectResponseDto;
import com.example.taskflow.mapper.EntityMapper;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public ProjectController(ProjectRepository projectRepository,
                             UserRepository userRepository,
                             EntityMapper entityMapper) {
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
    @Cacheable(value = "projects", key = "#authentication.name")
    public List<ProjectResponseDto> getUserProjects(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return projectRepository.findByOwnerId(user.getId()).stream()
                .map(entityMapper::toProjectDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return projectRepository.findById(id)
                .filter(project -> project.getOwner() != null && project.getOwner().getId().equals(user.getId()))
                .map(entityMapper::toProjectDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @CacheEvict(value = "projects", key = "#authentication.name")
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequestDto requestDto,
                                           Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            Project project = entityMapper.toProjectEntity(requestDto, user);
            Project savedProject = projectRepository.save(project);
            return ResponseEntity.ok(entityMapper.toProjectDto(savedProject));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Server Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "projects", key = "#authentication.name")
    public ResponseEntity<?> updateProject(@PathVariable Long id,
                                           @Valid @RequestBody ProjectRequestDto requestDto,
                                           Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return projectRepository.findById(id)
                .filter(project -> project.getOwner() != null && project.getOwner().getId().equals(user.getId()))
                .map(project -> {
                    project.setTitle(requestDto.title());
                    project.setDescription(requestDto.description());
                    Project updated = projectRepository.save(project);
                    return ResponseEntity.ok(entityMapper.toProjectDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "projects", key = "#authentication.name")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return projectRepository.findById(id)
                .filter(project -> project.getOwner() != null && project.getOwner().getId().equals(user.getId()))
                .map(project -> {
                    projectRepository.delete(project);
                    return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}