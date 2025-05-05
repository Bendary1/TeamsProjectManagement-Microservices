package com.TPM.project_management_service.controller;

import com.TPM.project_management_service.dto.ProjectMemberRequest;
import com.TPM.project_management_service.dto.ProjectMemberResponse;
import com.TPM.project_management_service.model.ProjectRole;
import com.TPM.project_management_service.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/members")
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> inviteMember(
            @PathVariable Long projectId,
            @RequestBody ProjectMemberRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Inviting member to project {}", projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectMemberService.inviteMember(projectId, request, token));
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token) {
        log.info("Getting members for project {}", projectId);
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId, token));
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<ProjectMemberResponse> acceptInvitation(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token) {
        log.info("Accepting invitation to project {}", projectId);
        return ResponseEntity.ok(projectMemberService.acceptInvitation(projectId, token));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveProject(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token) {
        log.info("Leaving project {}", projectId);
        projectMemberService.leaveProject(projectId, token);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Integer userId,
            @RequestParam ProjectRole role,
            @RequestHeader("Authorization") String token) {
        log.info("Updating role for user {} in project {}", userId, projectId);
        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId, userId, role, token));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Integer userId,
            @RequestHeader("Authorization") String token) {
        log.info("Removing user {} from project {}", userId, projectId);
        projectMemberService.removeMember(projectId, userId, token);
        return ResponseEntity.noContent().build();
    }
}
