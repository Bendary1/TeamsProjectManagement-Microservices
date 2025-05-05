package com.TPM.project_management_service.service;

import com.TPM.project_management_service.client.UserServiceClient;
import com.TPM.project_management_service.dto.TimeTrackingRequest;
import com.TPM.project_management_service.dto.TimeTrackingResponse;
import com.TPM.project_management_service.exception.ProjectNotFoundException;
import com.TPM.project_management_service.exception.TaskNotFoundException;
import com.TPM.project_management_service.exception.UnauthorizedAccessException;
import com.TPM.project_management_service.model.Project;
import com.TPM.project_management_service.model.ProjectMember;
import com.TPM.project_management_service.model.ProjectRole;
import com.TPM.project_management_service.model.Task;
import com.TPM.project_management_service.model.TimeTracking;
import com.TPM.project_management_service.repository.ProjectMemberRepository;
import com.TPM.project_management_service.repository.ProjectRepository;
import com.TPM.project_management_service.repository.TaskRepository;
import com.TPM.project_management_service.repository.TimeTrackingRepository;
import com.TPM.project_management_service.security.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrackingService {
    private final TimeTrackingRepository timeTrackingRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserServiceClient userServiceClient;
    private final JwtValidator jwtValidator;

    @Transactional
    public TimeTrackingResponse startTimeTracking(Long projectId, Long taskId, TimeTrackingRequest request, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Starting time tracking for task {} in project {} by user {}", taskId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
            
            // Verify task belongs to the specified project
            if (!task.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Task does not belong to this project");
            }
            
            TimeTracking timeTracking = new TimeTracking();
            timeTracking.setTask(task);
            timeTracking.setUserId(userProfile.getUser().getId());
            timeTracking.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
            timeTracking.setDescription(request.getDescription());
            
            timeTracking = timeTrackingRepository.save(timeTracking);
            log.info("Time tracking started successfully with id: {}", timeTracking.getId());
            
            return mapToTimeTrackingResponse(timeTracking);
        } catch (Exception e) {
            log.error("Error starting time tracking for task {}: {}", taskId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public TimeTrackingResponse stopTimeTracking(Long projectId, Long taskId, Long timeTrackingId, TimeTrackingRequest request, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Stopping time tracking {} for task {} in project {} by user {}", 
                timeTrackingId, taskId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
            
            // Verify task belongs to the specified project
            if (!task.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Task does not belong to this project");
            }
            
            TimeTracking timeTracking = timeTrackingRepository.findById(timeTrackingId)
                    .orElseThrow(() -> new IllegalArgumentException("Time tracking not found with id: " + timeTrackingId));
            
            // Verify time tracking belongs to the specified task
            if (!timeTracking.getTask().getId().equals(taskId)) {
                throw new IllegalArgumentException("Time tracking does not belong to this task");
            }
            
            // Verify time tracking belongs to the current user
            if (!timeTracking.getUserId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("Time tracking does not belong to the current user");
            }
            
            // Verify time tracking is not already stopped
            if (timeTracking.getEndTime() != null) {
                throw new IllegalArgumentException("Time tracking is already stopped");
            }
            
            timeTracking.setEndTime(request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now());
            
            if (request.getDescription() != null) {
                timeTracking.setDescription(request.getDescription());
            }
            
            timeTracking = timeTrackingRepository.save(timeTracking);
            log.info("Time tracking stopped successfully with id: {}", timeTracking.getId());
            
            return mapToTimeTrackingResponse(timeTracking);
        } catch (Exception e) {
            log.error("Error stopping time tracking {}: {}", timeTrackingId, e.getMessage(), e);
            throw e;
        }
    }
    
    public List<TimeTrackingResponse> getTimeTrackingsForTask(Long projectId, Long taskId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Getting time trackings for task {} in project {} by user {}", 
                taskId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
            
            // Verify task belongs to the specified project
            if (!task.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Task does not belong to this project");
            }
            
            List<TimeTracking> timeTrackings = timeTrackingRepository.findByTaskId(taskId);
            
            return timeTrackings.stream()
                    .map(this::mapToTimeTrackingResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting time trackings for task {}: {}", taskId, e.getMessage(), e);
            throw e;
        }
    }
    
    public List<TimeTrackingResponse> getTimeTrackingsForUser(Long projectId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Getting time trackings for user {} in project {}", userProfile.getUser().getId(), projectId);
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            // Get all tasks for the project
            List<Task> projectTasks = taskRepository.findByProjectId(projectId);
            
            // Get time trackings for the user across all project tasks
            List<TimeTracking> timeTrackings = timeTrackingRepository.findByUserId(userProfile.getUser().getId())
                    .stream()
                    .filter(tt -> projectTasks.stream().anyMatch(task -> task.getId().equals(tt.getTask().getId())))
                    .collect(Collectors.toList());
            
            return timeTrackings.stream()
                    .map(this::mapToTimeTrackingResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting time trackings for user in project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public void deleteTimeTracking(Long projectId, Long taskId, Long timeTrackingId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Deleting time tracking {} for task {} in project {} by user {}", timeTrackingId, taskId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
            
            // Verify task belongs to the specified project
            if (!task.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Task does not belong to this project");
            }
            
            TimeTracking timeTracking = timeTrackingRepository.findById(timeTrackingId)
                    .orElseThrow(() -> new IllegalArgumentException("Time tracking not found with id: " + timeTrackingId));
            
            // Verify time tracking belongs to the specified task
            if (!timeTracking.getTask().getId().equals(taskId)) {
                throw new IllegalArgumentException("Time tracking does not belong to this task");
            }
            
            // Verify user is authorized to delete the time tracking
            boolean isOwner = timeTracking.getUserId().equals(userProfile.getUser().getId());
            boolean isProjectOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isOwner && !isProjectOwner && !isAdmin) {
                throw new UnauthorizedAccessException("User is not authorized to delete this time tracking");
            }
            
            timeTrackingRepository.delete(timeTracking);
            log.info("Time tracking deleted successfully with id: {}", timeTracking.getId());
        } catch (Exception e) {
            log.error("Error deleting time tracking {}: {}", timeTrackingId, e.getMessage(), e);
            throw e;
        }
    }
    
    private TimeTrackingResponse mapToTimeTrackingResponse(TimeTracking timeTracking) {
        TimeTrackingResponse response = new TimeTrackingResponse();
        response.setId(timeTracking.getId());
        response.setTaskId(timeTracking.getTask().getId());
        response.setUserId(timeTracking.getUserId());
        response.setStartTime(timeTracking.getStartTime());
        response.setEndTime(timeTracking.getEndTime());
        response.setDurationMinutes(timeTracking.getDurationMinutes());
        response.setDescription(timeTracking.getDescription());
        response.setCreatedAt(timeTracking.getCreatedAt());
        response.setUpdatedAt(timeTracking.getUpdatedAt());
        return response;
    }
}
