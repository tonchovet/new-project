package com.example.demo.service;

import com.example.demo.dto.ProjectDto;
import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project createProject(ProjectDto dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setTargetAmount(dto.getTargetAmount());
        return projectRepository.save(project);
    }
}
