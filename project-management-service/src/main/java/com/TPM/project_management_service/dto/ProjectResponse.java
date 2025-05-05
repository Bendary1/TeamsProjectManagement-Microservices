package com.TPM.project_management_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Integer ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Integer> memberIds;
    private Set<Integer> adminIds;
}