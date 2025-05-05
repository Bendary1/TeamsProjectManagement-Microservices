package com.TPM.project_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private Set<Integer> memberIds;

    private Set<Integer> adminIds;
}