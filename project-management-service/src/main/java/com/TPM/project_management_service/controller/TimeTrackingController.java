package com.TPM.project_management_service.controller;

import com.TPM.project_management_service.dto.TimeTrackingRequest;
import com.TPM.project_management_service.dto.TimeTrackingResponse;
import com.TPM.project_management_service.service.TimeTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
@Slf4j
public class TimeTrackingController {
    private final TimeTrackingService timeTrackingService;

    @PostMapping("/tasks/{taskId}/time-tracking")
    public ResponseEntity<TimeTrackingResponse> startTimeTracking(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TimeTrackingRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Starting time tracking for task {} in project {}", taskId, projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeTrackingService.startTimeTracking(projectId, taskId, request, token));
    }

    @PutMapping("/tasks/{taskId}/time-tracking/{timeTrackingId}")
    public ResponseEntity<TimeTrackingResponse> stopTimeTracking(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long timeTrackingId,
            @RequestBody TimeTrackingRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Stopping time tracking {} for task {} in project {}", timeTrackingId, taskId, projectId);
        return ResponseEntity.ok(timeTrackingService.stopTimeTracking(projectId, taskId, timeTrackingId, request, token));
    }

    @GetMapping("/tasks/{taskId}/time-tracking")
    public ResponseEntity<List<TimeTrackingResponse>> getTimeTrackingsForTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        log.info("Getting time trackings for task {} in project {}", taskId, projectId);
        return ResponseEntity.ok(timeTrackingService.getTimeTrackingsForTask(projectId, taskId, token));
    }

    @GetMapping("/time-tracking/my")
    public ResponseEntity<List<TimeTrackingResponse>> getMyTimeTrackings(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token) {
        log.info("Getting my time trackings for project {}", projectId);
        return ResponseEntity.ok(timeTrackingService.getTimeTrackingsForUser(projectId, token));
    }

    @DeleteMapping("/tasks/{taskId}/time-tracking/{timeTrackingId}")
    public ResponseEntity<Void> deleteTimeTracking(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long timeTrackingId,
            @RequestHeader("Authorization") String token) {
        log.info("Deleting time tracking {} for task {} in project {}", timeTrackingId, taskId, projectId);
        timeTrackingService.deleteTimeTracking(projectId, taskId, timeTrackingId, token);
        return ResponseEntity.noContent().build();
    }
}
