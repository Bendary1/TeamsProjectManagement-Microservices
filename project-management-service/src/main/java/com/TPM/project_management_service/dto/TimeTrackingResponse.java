package com.TPM.project_management_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeTrackingResponse {
    private Long id;
    private Long taskId;
    private Integer userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
