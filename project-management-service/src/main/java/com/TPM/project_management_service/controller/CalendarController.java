package com.TPM.project_management_service.controller;

import com.TPM.project_management_service.dto.CalendarEventRequest;
import com.TPM.project_management_service.dto.CalendarEventResponse;
import com.TPM.project_management_service.model.ProjectCalendar;
import com.TPM.project_management_service.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/calendar")
@RequiredArgsConstructor
@Slf4j
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping
    public ResponseEntity<ProjectCalendar> createCalendar(
            @PathVariable Long projectId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestHeader("Authorization") String token) {
        log.info("Creating calendar for project {}", projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calendarService.createProjectCalendar(projectId, name, description, token));
    }

    @PostMapping("/events")
    public ResponseEntity<CalendarEventResponse> addEvent(
            @PathVariable Long projectId,
            @RequestBody CalendarEventRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Adding event to project calendar {}", projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calendarService.addEvent(projectId, request, token));
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventResponse>> getEvents(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestHeader("Authorization") String token) {
        log.info("Getting events for project calendar {}", projectId);
        return ResponseEntity.ok(calendarService.getCalendarEvents(projectId, start, end, token));
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<CalendarEventResponse> updateEvent(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @RequestBody CalendarEventRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Updating event {} for project calendar {}", eventId, projectId);
        return ResponseEntity.ok(calendarService.updateEvent(projectId, eventId, request, token));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        log.info("Deleting event {} from project calendar {}", eventId, projectId);
        calendarService.deleteEvent(projectId, eventId, token);
        return ResponseEntity.noContent().build();
    }
}
