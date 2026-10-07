package com.example.taskflow.controller;

import com.example.taskflow.model.Project;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Project project) {
        try {
            // Ensure a user exists to satisfy the foreign key constraint
            // Ensure a user exists to satisfy the foreign key constraint
            User owner = project.getOwner();
            if (owner == null || owner.getId() == null) {
                owner = userRepository.findAll().stream().findFirst().orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("Default User");
                    newUser.setEmail("default@taskflow.com"); // Satisfies @NotBlank for email
                    newUser.setPassword("defaultPassword123"); // Satisfies @NotBlank for password
                    return userRepository.save(newUser);
                });
                project.setOwner(owner);
            }

            Project savedProject = projectRepository.save(project);
            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Server Error: " + e.getMessage());
        }
    }
}