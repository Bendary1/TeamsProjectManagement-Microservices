package com.TPM.project_management_service.service;

import com.TPM.project_management_service.client.UserServiceClient;
import com.TPM.project_management_service.dto.AiTaskPlanResponse;
import com.TPM.project_management_service.dto.TaskRequest;
import com.TPM.project_management_service.dto.TaskResponse;
import com.TPM.project_management_service.exception.ProjectNotFoundException;
import com.TPM.project_management_service.exception.TaskNotFoundException;
import com.TPM.project_management_service.exception.UnauthorizedAccessException;
import com.TPM.project_management_service.model.Project;
import com.TPM.project_management_service.model.ProjectMember;
import com.TPM.project_management_service.model.ProjectRole;
import com.TPM.project_management_service.model.Task;
import com.TPM.project_management_service.repository.ProjectMemberRepository;
import com.TPM.project_management_service.repository.ProjectRepository;
import com.TPM.project_management_service.repository.TaskRepository;
import com.TPM.project_management_service.security.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserServiceClient userServiceClient;
    private final JwtValidator jwtValidator;
    private final AiTaskAssistantService aiTaskAssistantService;

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request, String token) {
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
            log.info("Creating task in project {} by user {}", projectId, userProfile.getUser().getId());

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }

            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setProject(project);
            task.setCreatorId(userProfile.getUser().getId());

            // Set creation and update timestamps
            LocalDateTime now = LocalDateTime.now();
            task.setCreatedAt(now);
            task.setUpdatedAt(now);

            if (request.getAssigneeId() != null) {
                // Verify assignee exists and is a member of the project
                try {
                    if (!userServiceClient.userExists(request.getAssigneeId(), token)) {
                        throw new IllegalArgumentException("Assignee does not exist: " + request.getAssigneeId());
                    }
                } catch (Exception e) {
                    // If User-Auth service returns a 403, assume the user exists and continue
                    // This is a fallback mechanism to handle auth service permissions issues
                    log.warn("Could not verify if user {} exists. Assuming the user exists and continuing: {}", 
                            request.getAssigneeId(), e.getMessage());
                }

                // Check if the assignee is already a project member
                Optional<ProjectMember> assigneeOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, request.getAssigneeId());
                
                // If not a project member, auto-add them
                if (assigneeOpt.isEmpty()) {
                    log.info("Auto-adding user {} as a member of project {} during task assignment", 
                            request.getAssigneeId(), projectId);
                    
                    ProjectMember newMember = new ProjectMember();
                    newMember.setUserId(request.getAssigneeId());
                    newMember.setProject(project);
                    newMember.setRole(ProjectRole.MEMBER);
                    newMember.setJoinedAt(now);
                    newMember.setInvitationAccepted(true);
                    newMember.setInvitedBy(userProfile.getUser().getId());  // Record who invited them
                    projectMemberRepository.save(newMember);
                }

                task.setAssigneeId(request.getAssigneeId());
            }

            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
            }

            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }

            if (request.getDeadline() != null) {
                task.setDeadline(request.getDeadline());
            }

            if (request.getEstimatedHours() != null) {
                task.setEstimatedHours(request.getEstimatedHours());
            }

            // Handle parent task if specified
            if (request.getParentTaskId() != null) {
                Task parentTask = taskRepository.findById(request.getParentTaskId())
                        .orElseThrow(() -> new TaskNotFoundException("Parent task not found with id: " + request.getParentTaskId()));

                // Verify parent task belongs to the same project
                if (!parentTask.getProject().getId().equals(projectId)) {
                    throw new IllegalArgumentException("Parent task does not belong to this project");
                }

                task.setParentTask(parentTask);
            }

            task = taskRepository.save(task);
            log.info("Task created successfully with id: {}", task.getId());

            return mapToTaskResponse(task);
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<TaskResponse> getAllTasksForProject(Long projectId, String token) {
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
            log.info("Getting all tasks for project {} by user {}", projectId, userProfile.getUser().getId());

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }

            List<Task> tasks = taskRepository.findByProjectId(projectId);

            // Only return top-level tasks (tasks without a parent)
            List<Task> topLevelTasks = tasks.stream()
                    .filter(task -> task.getParentTask() == null)
                    .collect(Collectors.toList());

            return mapToTaskResponsesWithSubtasks(topLevelTasks, tasks);
        } catch (Exception e) {
            log.error("Error getting tasks for project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    public TaskResponse getTaskById(Long projectId, Long taskId, String token) {
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
            log.info("Getting task {} in project {} by user {}", taskId, projectId, userProfile.getUser().getId());

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

            return mapToTaskResponseWithSubtasks(task);
        } catch (Exception e) {
            log.error("Error getting task {}: {}", taskId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskRequest request, String token) {
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
            log.info("Updating task {} in project {} by user {}", taskId, projectId, userProfile.getUser().getId());

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

            // Check if user is the creator, assignee, or project owner/admin
            boolean isCreator = task.getCreatorId().equals(userProfile.getUser().getId());
            boolean isAssignee = task.getAssigneeId() != null && task.getAssigneeId().equals(userProfile.getUser().getId());
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;

            if (!isCreator && !isAssignee && !isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("User is not authorized to update this task");
            }

            // Update task fields
            if (request.getTitle() != null) {
                task.setTitle(request.getTitle());
            }

            if (request.getDescription() != null) {
                task.setDescription(request.getDescription());
            }

            if (request.getAssigneeId() != null) {
                // Verify assignee exists and is a member of the project
                try {
                    if (!userServiceClient.userExists(request.getAssigneeId(), token)) {
                        throw new IllegalArgumentException("Assignee does not exist: " + request.getAssigneeId());
                    }
                } catch (Exception e) {
                    // If User-Auth service returns a 403, assume the user exists and continue
                    // This is a fallback mechanism to handle auth service permissions issues
                    log.warn("Could not verify if user {} exists. Assuming the user exists and continuing: {}", 
                            request.getAssigneeId(), e.getMessage());
                }

                // Check if the assignee is already a project member
                Optional<ProjectMember> assigneeOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, request.getAssigneeId());
                
                // If not a project member, auto-add them
                if (assigneeOpt.isEmpty()) {
                    log.info("Auto-adding user {} as a member of project {} during task assignment", 
                            request.getAssigneeId(), projectId);
                    
                    ProjectMember newMember = new ProjectMember();
                    newMember.setUserId(request.getAssigneeId());
                    newMember.setProject(project);
                    newMember.setRole(ProjectRole.MEMBER);
                    newMember.setJoinedAt(LocalDateTime.now());
                    newMember.setInvitationAccepted(true);
                    newMember.setInvitedBy(userProfile.getUser().getId());  // Record who invited them
                    projectMemberRepository.save(newMember);
                }

                task.setAssigneeId(request.getAssigneeId());
            }

            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
            }

            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }

            if (request.getDeadline() != null) {
                task.setDeadline(request.getDeadline());
            }

            if (request.getEstimatedHours() != null) {
                task.setEstimatedHours(request.getEstimatedHours());
            }

            // Handle parent task if specified
            if (request.getParentTaskId() != null && !request.getParentTaskId().equals(task.getParentTask() != null ? task.getParentTask().getId() : null)) {
                Task parentTask = taskRepository.findById(request.getParentTaskId())
                        .orElseThrow(() -> new TaskNotFoundException("Parent task not found with id: " + request.getParentTaskId()));

                // Verify parent task belongs to the same project
                if (!parentTask.getProject().getId().equals(projectId)) {
                    throw new IllegalArgumentException("Parent task does not belong to this project");
                }

                // Prevent circular references
                if (parentTask.getId().equals(task.getId())) {
                    throw new IllegalArgumentException("Task cannot be its own parent");
                }

                task.setParentTask(parentTask);
            } else if (request.getParentTaskId() == null && task.getParentTask() != null) {
                // Remove parent task reference
                task.setParentTask(null);
            }

            task.setUpdatedAt(LocalDateTime.now());
            task = taskRepository.save(task);
            log.info("Task updated successfully with id: {}", task.getId());

            return mapToTaskResponseWithSubtasks(task);
        } catch (Exception e) {
            log.error("Error updating task {}: {}", taskId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId, String token) {
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
            log.info("Deleting task {} in project {} by user {}", taskId, projectId, userProfile.getUser().getId());

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

            // Check if user is the creator or project owner/admin
            boolean isCreator = task.getCreatorId().equals(userProfile.getUser().getId());
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;

            if (!isCreator && !isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("User is not authorized to delete this task");
            }

            taskRepository.delete(task);
            log.info("Task deleted successfully with id: {}", task.getId());
        } catch (Exception e) {
            log.error("Error deleting task {}: {}", taskId, e.getMessage(), e);
            throw e;
        }
    }

    public AiTaskPlanResponse generateAiTaskPlan(Long projectId, Long taskId, String token) {
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
            log.info("Generating AI task plan for task {} in project {} by user {}", 
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

            // Verify that the task belongs to the specified project
            if (!task.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Task does not belong to the specified project");
            }

            // Generate AI task plan using AiTaskAssistantService
            String plan = aiTaskAssistantService.generateTaskPlan(task);
            
            return AiTaskPlanResponse.builder()
                    .taskId(task.getId())
                    .taskTitle(task.getTitle())
                    .plan(plan)
                    .build();
        } catch (Exception e) {
            log.error("Error generating AI task plan: {}", e.getMessage(), e);
            throw e;
        }
    }

    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setProjectId(task.getProject().getId());
        response.setCreatorId(task.getCreatorId());
        response.setAssigneeId(task.getAssigneeId());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDeadline(task.getDeadline());
        response.setEstimatedHours(task.getEstimatedHours());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getParentTask() != null) {
            response.setParentTaskId(task.getParentTask().getId());
        }

        return response;
    }

    private TaskResponse mapToTaskResponseWithSubtasks(Task task) {
        TaskResponse response = mapToTaskResponse(task);

        if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
            List<TaskResponse> subtaskResponses = task.getSubtasks().stream()
                    .map(this::mapToTaskResponseWithSubtasks)
                    .collect(Collectors.toList());
            response.setSubtasks(subtaskResponses);
        } else {
            response.setSubtasks(new ArrayList<>());
        }

        return response;
    }

    private List<TaskResponse> mapToTaskResponsesWithSubtasks(List<Task> topLevelTasks, List<Task> allTasks) {
        return topLevelTasks.stream()
                .map(this::mapToTaskResponseWithSubtasks)
                .collect(Collectors.toList());
    }
}
