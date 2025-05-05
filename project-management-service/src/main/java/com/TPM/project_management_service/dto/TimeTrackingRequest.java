package com.TPM.project_management_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeTrackingRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
}
