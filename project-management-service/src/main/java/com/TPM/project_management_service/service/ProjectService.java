package com.TPM.project_management_service.service;

import com.TPM.project_management_service.client.UserServiceClient;
import com.TPM.project_management_service.dto.ProjectRequest;
import com.TPM.project_management_service.dto.ProjectResponse;
import com.TPM.project_management_service.exception.ProjectNotFoundException;
import com.TPM.project_management_service.exception.UnauthorizedAccessException;
import com.TPM.project_management_service.model.Project;
import com.TPM.project_management_service.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String token) {
        try {
            log.info("Starting project creation process");
            log.info("Request data: {}", request);
            log.info("Token: {}", token);

            // Verify user profile
            log.info("Attempting to get user profile");
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("User profile retrieved successfully. User ID: {}, Email: {}",
                    userProfile.getUser().getId(),
                    userProfile.getUser().getEmail());

            // Create project entity
            log.info("Creating project entity");
            Project project = new Project();
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setOwnerId(userProfile.getUser().getId());  // Use authenticated user's ID as owner

            // Add members and admins without verification
            if (request.getMemberIds() != null) {
                log.info("Adding members to project: {}", request.getMemberIds());
                project.setMemberIds(request.getMemberIds());
            }

            if (request.getAdminIds() != null) {
                log.info("Adding admins to project: {}", request.getAdminIds());
                project.setAdminIds(request.getAdminIds());
            }

            // Save project
            log.info("Attempting to save project to database");
            Project savedProject = projectRepository.save(project);
            log.info("Project saved successfully with ID: {}", savedProject.getId());

            // Convert to response
            log.info("Converting project to response");
            return mapToResponse(savedProject);
        } catch (Exception e) {
            log.error("Error creating project: {}", e.getMessage(), e);
            log.error("Stack trace: ", e);
            throw new RuntimeException("Failed to create project: " + e.getMessage());
        }
    }

    public List<ProjectResponse> getAllProjectsForUser(String token) {
        try {
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Getting all projects for user in service layer: {}", userProfile.getUser().getEmail());

            List<Project> projects = projectRepository.findByOwnerId(userProfile.getUser().getId());
            projects.addAll(projectRepository.findByMemberIdsContaining(userProfile.getUser().getId()));
            projects.addAll(projectRepository.findByAdminIdsContaining(userProfile.getUser().getId()));

            log.info("Found {} projects for user: {}", projects.size(), userProfile.getUser().getEmail());
            return projects.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting projects: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ProjectResponse getProjectById(Long id, String token) {
        try {
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Getting project by id in service layer - id: {}, userId: {}", id, userProfile.getUser().getId());

            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Project not found with id: {}", id);
                        return new ProjectNotFoundException("Project not found with id: " + id);
                    });

            if (!isUserAuthorized(project, userProfile.getUser().getId())) {
                log.error("Unauthorized access attempt - userId: {}, projectId: {}", userProfile.getUser().getId(), id);
                throw new UnauthorizedAccessException("User is not authorized to access this project");
            }

            return mapToResponse(project);
        } catch (Exception e) {
            log.error("Error getting project: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String token) {
        try {
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Updating project in service layer - id: {}, userId: {}", id, userProfile.getUser().getId());

            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Project not found with id: {}", id);
                        return new ProjectNotFoundException("Project not found with id: " + id);
                    });

            if (!isUserAuthorized(project, userProfile.getUser().getId())) {
                log.error("Unauthorized update attempt - userId: {}, projectId: {}", userProfile.getUser().getId(), id);
                throw new UnauthorizedAccessException("User is not authorized to update this project");
            }

            project.setName(request.getName());
            project.setDescription(request.getDescription());

            if (request.getMemberIds() != null) {
                // Verify each member exists
                for (Integer memberId : request.getMemberIds()) {
                    try {
                        if (!userServiceClient.userExists(memberId, token)) {
                            log.error("Member does not exist: {}", memberId);
                            throw new IllegalArgumentException("Member does not exist: " + memberId);
                        }
                    } catch (Exception e) {
                        log.error("Error verifying member: {}", memberId, e);
                        throw new IllegalArgumentException("Error verifying member: " + memberId);
                    }
                }
                project.setMemberIds(request.getMemberIds());
            }

            if (request.getAdminIds() != null) {
                // Verify each admin exists
                for (Integer adminId : request.getAdminIds()) {
                    try {
                        if (!userServiceClient.userExists(adminId, token)) {
                            log.error("Admin does not exist: {}", adminId);
                            throw new IllegalArgumentException("Admin does not exist: " + adminId);
                        }
                    } catch (Exception e) {
                        log.error("Error verifying admin: {}", adminId, e);
                        throw new IllegalArgumentException("Error verifying admin: " + adminId);
                    }
                }
                project.setAdminIds(request.getAdminIds());
            }

            Project updatedProject = projectRepository.save(project);
            log.info("Project updated successfully - id: {}", id);
            return mapToResponse(updatedProject);
        } catch (Exception e) {
            log.error("Error updating project: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteProject(Long id, String token) {
        try {
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Deleting project in service layer - id: {}, userId: {}", id, userProfile.getUser().getId());

            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Project not found with id: {}", id);
                        return new ProjectNotFoundException("Project not found with id: " + id);
                    });

            if (!project.getOwnerId().equals(userProfile.getUser().getId())) {
                log.error("Unauthorized delete attempt - userId: {}, projectId: {}", userProfile.getUser().getId(), id);
                throw new UnauthorizedAccessException("Only project owner can delete the project");
            }

            projectRepository.delete(project);
            log.info("Project deleted successfully - id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting project: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isUserAuthorized(Project project, Integer userId) {
        return project.getOwnerId().equals(userId) ||
               project.getMemberIds().contains(userId) ||
               project.getAdminIds().contains(userId);
    }

    private ProjectResponse mapToResponse(Project project) {
        log.info("Mapping project to response. Project ID: {}", project.getId());
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setOwnerId(project.getOwnerId());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setMemberIds(project.getMemberIds());
        response.setAdminIds(project.getAdminIds());
        log.info("Project mapped successfully. Response: {}", response);
        return response;
    }
}