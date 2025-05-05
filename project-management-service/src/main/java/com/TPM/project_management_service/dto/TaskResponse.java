package com.TPM.project_management_service.dto;

import com.TPM.project_management_service.model.TaskPriority;
import com.TPM.project_management_service.model.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private Integer creatorId;
    private Integer assigneeId;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime deadline;
    private Integer estimatedHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentTaskId;
    private List<TaskResponse> subtasks;
}
