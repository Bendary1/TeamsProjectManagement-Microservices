package com.TPM.project_management_service.dto;

import com.TPM.project_management_service.model.TaskPriority;
import com.TPM.project_management_service.model.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private Integer assigneeId;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime deadline;
    private Integer estimatedHours;
    private Long parentTaskId;
}
