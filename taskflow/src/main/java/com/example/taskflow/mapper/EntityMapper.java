package com.example.taskflow.mapper;

import com.example.taskflow.dto.ProjectRequestDto;
import com.example.taskflow.dto.ProjectResponseDto;
import com.example.taskflow.dto.TaskRequestDto;
import com.example.taskflow.dto.TaskResponseDto;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.Task;
import com.example.taskflow.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public TaskResponseDto toTaskDto(Task task) {
        if (task == null) return null;

        Long projectId = (task.getProject() != null) ? task.getProject().getId() : null;
        String projectTitle = (task.getProject() != null) ? task.getProject().getTitle() : null;

        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                projectId,
                projectTitle
        );
    }

    public ProjectResponseDto toProjectDto(Project project) {
        if (project == null) return null;

        List<TaskResponseDto> taskDtos = null;
        if (project.getTasks() != null) {
            taskDtos = project.getTasks().stream()
                    .map(this::toTaskDto)
                    .collect(Collectors.toList());
        }

        return new ProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                taskDtos
        );
    }

    public Project toProjectEntity(ProjectRequestDto dto, User owner) {
        if (dto == null) return null;
        Project project = new Project();
        project.setTitle(dto.title());
        project.setDescription(dto.description());
        project.setOwner(owner);
        return project;
    }

    public Task toTaskEntity(TaskRequestDto dto, Project project) {
        if (dto == null) return null;
        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setPriority(dto.priority());
        task.setProject(project);
        return task;
    }
}