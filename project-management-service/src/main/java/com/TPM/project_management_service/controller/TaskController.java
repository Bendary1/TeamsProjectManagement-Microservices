package com.TPM.project_management_service.controller;

import com.TPM.project_management_service.dto.AiTaskPlanResponse;
import com.TPM.project_management_service.dto.ErrorResponse;
import com.TPM.project_management_service.dto.TaskRequest;
import com.TPM.project_management_service.dto.TaskResponse;
import com.TPM.project_management_service.exception.ProjectNotFoundException;
import com.TPM.project_management_service.exception.TaskNotFoundException;
import com.TPM.project_management_service.exception.UnauthorizedAccessException;
import com.TPM.project_management_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Object> createTask(
            @PathVariable Long projectId,
            @RequestBody TaskRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Creating task in project {}", projectId);
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(taskService.createTask(projectId, request, token));
        } catch (ProjectNotFoundException e) {
            log.error("Project not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error creating task: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllTasks(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token) {
        log.info("Getting all tasks for project {}", projectId);
        try {
            return ResponseEntity.ok(taskService.getAllTasksForProject(projectId, token));
        } catch (ProjectNotFoundException e) {
            log.error("Project not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error getting tasks: " + e.getMessage()));
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Object> getTaskById(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        log.info("Getting task {} for project {}", taskId, projectId);
        try {
            return ResponseEntity.ok(taskService.getTaskById(projectId, taskId, token));
        } catch (ProjectNotFoundException | TaskNotFoundException e) {
            log.error("Not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error getting task: " + e.getMessage()));
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Object> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Updating task {} for project {}", taskId, projectId);
        try {
            return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request, token));
        } catch (ProjectNotFoundException | TaskNotFoundException e) {
            log.error("Not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error updating task: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Object> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        log.info("Deleting task {} for project {}", taskId, projectId);
        try {
            taskService.deleteTask(projectId, taskId, token);
            return ResponseEntity.noContent().build();
        } catch (ProjectNotFoundException | TaskNotFoundException e) {
            log.error("Not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error deleting task: " + e.getMessage()));
        }
    }

    @GetMapping("/{taskId}/ai-plan")
    public ResponseEntity<Object> generateAiTaskPlan(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        log.info("Generating AI plan for task {} in project {}", taskId, projectId);
        try {
            return ResponseEntity.ok(taskService.generateAiTaskPlan(projectId, taskId, token));
        } catch (ProjectNotFoundException | TaskNotFoundException e) {
            log.error("Not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating AI task plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating AI task plan: " + e.getMessage()));
        }
    }
}
