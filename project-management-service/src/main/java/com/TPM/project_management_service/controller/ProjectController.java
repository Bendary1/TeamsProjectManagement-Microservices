package com.TPM.project_management_service.controller;

import com.TPM.project_management_service.dto.ProjectRequest;
import com.TPM.project_management_service.dto.ProjectResponse;
import com.TPM.project_management_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Creating project with name: {}", request.getName());
        ProjectResponse response = projectService.createProject(request, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjectsForUser(
            @RequestHeader("Authorization") String token) {
        log.info("Getting all projects for user");
        List<ProjectResponse> projects = projectService.getAllProjectsForUser(token);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        log.info("Getting project with id: {}", id);
        ProjectResponse project = projectService.getProjectById(id, token);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Updating project with id: {}", id);
        ProjectResponse updatedProject = projectService.updateProject(id, request, token);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        log.info("Deleting project with id: {}", id);
        projectService.deleteProject(id, token);
        return ResponseEntity.noContent().build();
    }
} 