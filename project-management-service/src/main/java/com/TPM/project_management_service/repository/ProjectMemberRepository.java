package com.TPM.project_management_service.repository;

import com.TPM.project_management_service.model.ProjectMember;
import com.TPM.project_management_service.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByUserId(Integer userId);
    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Integer userId);
    List<ProjectMember> findByInvitedByAndInvitationAccepted(Integer invitedBy, Boolean invitationAccepted);
}
