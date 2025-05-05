package com.TPM.project_management_service.dto;

import com.TPM.project_management_service.model.ProjectRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectMemberResponse {
    private Long id;
    private Long projectId;
    private Integer userId;
    private ProjectRole role;
    private LocalDateTime joinedAt;
    private Integer invitedBy;
    private Boolean invitationAccepted;
}
