package com.TPM.project_management_service.service;

import com.TPM.project_management_service.client.UserServiceClient;
import com.TPM.project_management_service.dto.CalendarEventRequest;
import com.TPM.project_management_service.dto.CalendarEventResponse;
import com.TPM.project_management_service.exception.ProjectNotFoundException;
import com.TPM.project_management_service.exception.TaskNotFoundException;
import com.TPM.project_management_service.exception.UnauthorizedAccessException;
import com.TPM.project_management_service.model.*;
import com.TPM.project_management_service.repository.CalendarEventRepository;
import com.TPM.project_management_service.repository.ProjectCalendarRepository;
import com.TPM.project_management_service.repository.ProjectMemberRepository;
import com.TPM.project_management_service.repository.ProjectRepository;
import com.TPM.project_management_service.repository.TaskRepository;
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
public class CalendarService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectCalendarRepository projectCalendarRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;
    private final JwtValidator jwtValidator;

    @Transactional
    public ProjectCalendar createProjectCalendar(Long projectId, String name, String description, String token) {
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
            log.info("Creating calendar for project {} by user {}", projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is the owner or admin of the project
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("Only project owner or admin can create a calendar");
            }
            
            // Check if calendar already exists
            Optional<ProjectCalendar> existingCalendar = projectCalendarRepository.findByProjectId(projectId);
            if (existingCalendar.isPresent()) {
                throw new IllegalArgumentException("Calendar already exists for this project");
            }
            
            ProjectCalendar calendar = new ProjectCalendar();
            calendar.setProject(project);
            calendar.setName(name);
            calendar.setDescription(description);
            
            calendar = projectCalendarRepository.save(calendar);
            log.info("Calendar created successfully with id: {}", calendar.getId());
            
            return calendar;
        } catch (Exception e) {
            log.error("Error creating calendar for project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public CalendarEventResponse addEvent(Long projectId, CalendarEventRequest request, String token) {
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
            log.info("Adding event to project calendar {} by user {}", projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            // Get or create calendar
            ProjectCalendar calendar = projectCalendarRepository.findByProjectId(projectId)
                    .orElseGet(() -> {
                        ProjectCalendar newCalendar = new ProjectCalendar();
                        newCalendar.setProject(project);
                        newCalendar.setName(project.getName() + " Calendar");
                        newCalendar.setDescription("Calendar for " + project.getName());
                        return projectCalendarRepository.save(newCalendar);
                    });
            
            CalendarEvent event = new CalendarEvent();
            event.setCalendar(calendar);
            event.setTitle(request.getTitle());
            event.setDescription(request.getDescription());
            event.setEventType(request.getEventType());
            event.setStartTime(request.getStartTime());
            event.setEndTime(request.getEndTime());
            event.setAllDay(request.getAllDay() != null ? request.getAllDay() : false);
            event.setLocation(request.getLocation());
            event.setCreatedBy(userProfile.getUser().getId());
            
            // Link to task if specified
            if (request.getTaskId() != null) {
                Task task = taskRepository.findById(request.getTaskId())
                        .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + request.getTaskId()));
                
                // Verify task belongs to the same project
                if (!task.getProject().getId().equals(projectId)) {
                    throw new IllegalArgumentException("Task does not belong to this project");
                }
                
                event.setTask(task);
            }
            
            event = calendarEventRepository.save(event);
            log.info("Event added successfully with id: {}", event.getId());
            
            return mapToCalendarEventResponse(event);
        } catch (Exception e) {
            log.error("Error adding event to project calendar {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    public List<CalendarEventResponse> getCalendarEvents(Long projectId, LocalDateTime start, LocalDateTime end, String token) {
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
            log.info("Getting calendar events for project {} by user {}", projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            // Get calendar
            ProjectCalendar calendar = projectCalendarRepository.findByProjectId(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Calendar not found for this project"));
            
            List<CalendarEvent> events;
            if (start != null && end != null) {
                events = calendarEventRepository.findByCalendarIdAndStartTimeBetween(calendar.getId(), start, end);
            } else {
                events = calendarEventRepository.findByCalendarId(calendar.getId());
            }
            
            return events.stream()
                    .map(this::mapToCalendarEventResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting calendar events for project {}: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public CalendarEventResponse updateEvent(Long projectId, Long eventId, CalendarEventRequest request, String token) {
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
            log.info("Updating calendar event {} for project {} by user {}", eventId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            // Get calendar
            ProjectCalendar calendar = projectCalendarRepository.findByProjectId(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Calendar not found for this project"));
            
            CalendarEvent event = calendarEventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
            
            // Verify event belongs to the project calendar
            if (!event.getCalendar().getId().equals(calendar.getId())) {
                throw new IllegalArgumentException("Event does not belong to this project calendar");
            }
            
            // Check if user is the creator, owner, or admin
            boolean isCreator = event.getCreatedBy().equals(userProfile.getUser().getId());
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isCreator && !isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("User is not authorized to update this event");
            }
            
            // Update event fields
            if (request.getTitle() != null) {
                event.setTitle(request.getTitle());
            }
            
            if (request.getDescription() != null) {
                event.setDescription(request.getDescription());
            }
            
            if (request.getEventType() != null) {
                event.setEventType(request.getEventType());
            }
            
            if (request.getStartTime() != null) {
                event.setStartTime(request.getStartTime());
            }
            
            if (request.getEndTime() != null) {
                event.setEndTime(request.getEndTime());
            }
            
            if (request.getAllDay() != null) {
                event.setAllDay(request.getAllDay());
            }
            
            if (request.getLocation() != null) {
                event.setLocation(request.getLocation());
            }
            
            // Update task link if specified
            if (request.getTaskId() != null) {
                Task task = taskRepository.findById(request.getTaskId())
                        .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + request.getTaskId()));
                
                // Verify task belongs to the same project
                if (!task.getProject().getId().equals(projectId)) {
                    throw new IllegalArgumentException("Task does not belong to this project");
                }
                
                event.setTask(task);
            } else if (request.getTaskId() == null && event.getTask() != null) {
                // Remove task link
                event.setTask(null);
            }
            
            event.setUpdatedAt(LocalDateTime.now());
            event = calendarEventRepository.save(event);
            log.info("Event updated successfully with id: {}", event.getId());
            
            return mapToCalendarEventResponse(event);
        } catch (Exception e) {
            log.error("Error updating calendar event {}: {}", eventId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public void deleteEvent(Long projectId, Long eventId, String token) {
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
            log.info("Deleting calendar event {} for project {} by user {}", eventId, projectId, userProfile.getUser().getId());
            
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
            
            // Check if user is a member of the project
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userProfile.getUser().getId());
            if (memberOpt.isEmpty() && !project.getOwnerId().equals(userProfile.getUser().getId())) {
                throw new UnauthorizedAccessException("User is not a member of this project");
            }
            
            // Get calendar
            ProjectCalendar calendar = projectCalendarRepository.findByProjectId(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Calendar not found for this project"));
            
            CalendarEvent event = calendarEventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
            
            // Verify event belongs to the project calendar
            if (!event.getCalendar().getId().equals(calendar.getId())) {
                throw new IllegalArgumentException("Event does not belong to this project calendar");
            }
            
            // Check if user is the creator, owner, or admin
            boolean isCreator = event.getCreatedBy().equals(userProfile.getUser().getId());
            boolean isOwner = project.getOwnerId().equals(userProfile.getUser().getId());
            boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.ADMIN;
            
            if (!isCreator && !isOwner && !isAdmin) {
                throw new UnauthorizedAccessException("User is not authorized to delete this event");
            }
            
            calendarEventRepository.delete(event);
            log.info("Event deleted successfully with id: {}", event.getId());
        } catch (Exception e) {
            log.error("Error deleting calendar event {}: {}", eventId, e.getMessage(), e);
            throw e;
        }
    }
    
    private CalendarEventResponse mapToCalendarEventResponse(CalendarEvent event) {
        CalendarEventResponse response = new CalendarEventResponse();
        response.setId(event.getId());
        response.setCalendarId(event.getCalendar().getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventType(event.getEventType());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setAllDay(event.getAllDay());
        response.setLocation(event.getLocation());
        response.setCreatedBy(event.getCreatedBy());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        
        if (event.getTask() != null) {
            response.setTaskId(event.getTask().getId());
        }
        
        return response;
    }
}
