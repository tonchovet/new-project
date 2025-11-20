package com.example.demo.controller;

import com.example.demo.dto.ProjectDto;
import com.example.demo.model.Project;
import com.example.demo.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAll() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody ProjectDto dto) {
        return ResponseEntity.ok(projectService.createProject(dto));
    }
}
