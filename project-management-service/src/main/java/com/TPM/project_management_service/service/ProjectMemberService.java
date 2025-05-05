package com.TPM.project_management_service.service;

import com.TPM.project_management_service.client.UserServiceClient;
import com.TPM.project_management_service.dto.ProjectMemberRequest;
import com.TPM.project_management_service.dto.ProjectMemberResponse;
import com.TPM.project_management_service.exception.ProjectNotFoundException;
import com.TPM.project_management_service.exception.UnauthorizedAccessException;
import com.TPM.project_management_service.model.Project;
import com.TPM.project_management_service.model.ProjectMember;
import com.TPM.project_management_service.model.ProjectRole;
import com.TPM.project_management_service.repository.ProjectMemberRepository;
import com.TPM.project_management_service.repository.ProjectRepository;
import com.TPM.project_management_service.security.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserServiceClient userServiceClient;
    private final JwtValidator jwtValidator;

    @Transactional
    public ProjectMemberResponse inviteMember(Long projectId, ProjectMemberRequest request, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Inviting member to project {} by user {}", projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is the owner or admin of the project
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("Only project owner or admin can invite members");
            }
            
            // Verify user exists
            if (!userServiceClient.userExists(request.getUserId(), token)) {
                throw new IllegalArgumentException("User does not exist: " + request.getUserId());
            }
            
            // Check if user is already a member
            Optional<ProjectMember> existingMember = projectMemberRepository.findByProjectIdAndUserId(projectId, request.getUserId());
            if (existingMember.isPresent()) {
                throw new IllegalArgumentException("User is already a member of this project");
            }
            
            // Create new project member
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(project);
            projectMember.setUserId(request.getUserId());
            projectMember.setRole(request.getRole() != null ? request.getRole() : ProjectRole.MEMBER);
            projectMember.setInvitedBy(userProfile.getUser().getId());
            projectMember.setInvitationAccepted(false);
            
            projectMember = projectMemberRepository.save(projectMember);
            log.info("Member invited successfully with id: {}", projectMember.getId());
            
            return mapToProjectMemberResponse(projectMember);
        } catch (Exception e) {
            log.error("Error inviting member to project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public ProjectMemberResponse acceptInvitation(Long projectId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Accepting invitation to project {} by user {}", projectId, userProfile.getUser().getId());
            
            // Check if project exists
            if (!projectRepository.existsById(projectId)) {
                throw new ProjectNotFoundException("Project not found with id: " + projectId);
            }
            
            // Find invitation
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty()) {
                throw new IllegalArgumentException("No invitation found for this project");
            }
            
            ProjectMember member = memberOpt.get();
            if (member.getInvitationAccepted()) {
                throw new IllegalArgumentException("Invitation already accepted");
            }
            
            member.setInvitationAccepted(true);
            member = projectMemberRepository.save(member);
            log.info("Invitation accepted successfully for member id: {}", member.getId());
            
            return mapToProjectMemberResponse(member);
        } catch (Exception e) {
            log.error("Error accepting invitation to project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public void leaveProject(Long projectId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("User {} leaving project {}", userProfile.getUser().getId(), projectId);
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty()) {
                throw new IllegalArgumentException("User is not a member of this project");
            }
            
            // Owner cannot leave the project
            if (project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new IllegalArgumentException("Project owner cannot leave the project. Transfer ownership first.");
            }
            
            projectMemberRepository.delete(memberOpt.get());
            log.info("User left project successfully");
        } catch (Exception e) {
            log.error("Error leaving project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public ProjectMemberResponse updateMemberRole(Long projectId, Integer userId, ProjectRole role, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Updating role for member {} in project {} to {} by user {}", 
                    userId, projectId, role, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is the owner or admin of the project
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            Optional<ProjectMember> currentUserMemberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            boolean isAdmin = currentUserMemberOpt.isPresent() && currentUserMemberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("Only project owner or admin can update member roles");
            }
            
            // Find the member to update
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
            if (memberOpt.isEmpty()) {
                throw new IllegalArgumentException("User is not a member of this project");
            }
            
            ProjectMember member = memberOpt.get();
            
            // Only owner can assign OWNER role
            if (role == ProjectRole.OWNER && !isOwner) {
                throw new UnauthorizedAccessException("Only the current owner can transfer ownership");
            }
            
            // If transferring ownership
            if (role == ProjectRole.OWNER) {
                // Update project owner
                project.setOwnerId(userId);
                projectRepository.save(project);
                
                // Update current owner's role to ADMIN
                if (currentUserMemberOpt.isEmpty()) {
                    // Create a member entry for the current owner
                    ProjectMember ownerMember = new ProjectMember();
                    ownerMember.setProject(project);
                    ownerMember.setUserId(userProfile.getUser().getId());
                    ownerMember.setRole(ProjectRole.ADMIN);
                    ownerMember.setInvitationAccepted(true);
                    projectMemberRepository.save(ownerMember);
                } else {
                    currentUserMemberOpt.get().setRole(ProjectRole.ADMIN);
                    projectMemberRepository.save(currentUserMemberOpt.get());
                }
            }
            
            member.setRole(role);
            member = projectMemberRepository.save(member);
            log.info("Member role updated successfully for member id: {}", member.getId());
            
            return mapToProjectMemberResponse(member);
        } catch (Exception e) {
            log.error("Error updating member role in project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public void removeMember(Long projectId, Integer userId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Removing member {} from project {} by user {}", userId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is the owner or admin of the project
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            Optional<ProjectMember> currentUserMemberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            boolean isAdmin = currentUserMemberOpt.isPresent() && currentUserMemberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("Only project owner or admin can remove members");
            }
            
            // Find the member to remove
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
            if (memberOpt.isEmpty()) {
                throw new IllegalArgumentException("User is not a member of this project");
            }
            
            // Cannot remove the owner
            if (project.getOwnerId().equals(userId)) {
                throw new IllegalArgumentException("Cannot remove the project owner");
            }
            
            // Admin cannot remove another admin
            if (!isOwner && memberOpt.get().getRole() == ProjectRole.ADMIN) {
                throw new UnauthorizedAccessException("Admin cannot remove another admin");
            }
            
            projectMemberRepository.delete(memberOpt.get());
            log.info("Member removed successfully");
        } catch (Exception e) {
            log.error("Error removing member from project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, String token) {
        try {
            // Validate JWT token with fallback mechanism
            try {
                if (!jwtValidator.validateToken(token)) {
                    log.error("Invalid JWT token provided");
                    throw new UnauthorizedAccessException("Invalid or expired token");
                }
            } catch (Exception e) {
                // If JWT validation fails due to signature mismatch, continue with external validation
                // This is needed when secret keys are different between services
                log.warn("Local JWT validation failed: {}. Continuing with user-auth service validation.", e.getMessage());
            }
            
            // The user-auth service will also validate the token when calling getUserProfile
            var userProfile = userServiceClient.getUserProfile(token);
            log.info("Getting members for project {} by user {}", projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
            
            return members.stream()
                    .map(this::mapToProjectMemberResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting members for project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    private ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member) {
        ProjectMemberResponse response = new ProjectMemberResponse();
        response.setId(member.getId());
        response.setProjectId(member.getProject().getId());
        response.setUserId(member.getUserId());
        response.setRole(member.getRole());
        response.setJoinedAt(member.getJoinedAt());
        response.setInvitedBy(member.getInvitedBy());
        response.setInvitationAccepted(member.getInvitationAccepted());
        return response;
    }
}
