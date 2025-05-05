package com.TPM.project_management_service.dto;

import com.TPM.project_management_service.model.CalendarEventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarEventResponse {
    private Long id;
    private Long calendarId;
    private String title;
    private String description;
    private CalendarEventType eventType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean allDay;
    private String location;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long taskId;
}
