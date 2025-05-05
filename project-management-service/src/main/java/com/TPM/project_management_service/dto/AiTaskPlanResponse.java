package com.TPM.project_management_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTaskPlanResponse {
    private Long taskId;
    private String taskTitle;
    private String plan;
} 