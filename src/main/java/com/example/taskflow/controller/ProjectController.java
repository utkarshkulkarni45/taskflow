package com.example.taskflow.controller;

import com.example.taskflow.dto.ProjectRequestDto;
import com.example.taskflow.dto.ProjectResponseDto;
import com.example.taskflow.mapper.EntityMapper;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
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

    @GetMapping
    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(entityMapper::toProjectDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(entityMapper::toProjectDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody @Valid ProjectRequestDto requestDto) {
        User owner = userRepository.findById(requestDto.ownerId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = entityMapper.toProjectEntity(requestDto, owner);
        Project savedProject = projectRepository.save(project);

        return ResponseEntity.ok(entityMapper.toProjectDto(savedProject));
    }
}