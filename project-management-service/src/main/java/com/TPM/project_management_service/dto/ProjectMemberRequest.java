package com.TPM.project_management_service.dto;

import com.TPM.project_management_service.model.ProjectRole;
import lombok.Data;

@Data
public class ProjectMemberRequest {
    private Integer userId;
    private ProjectRole role;
}
