# Project Management Service

This service handles project management, task management, team membership, and calendar integration for the Teams Project Management system.

## Features

### Project Management
- Create, update, and delete projects
- Assign project members and admins
- Set project ownership

### Task Management
- Create, update, and delete tasks within projects
- Set task deadlines, priorities, and assign members
- Support for subtasks (parent-child relationship)
- Track task status (TODO, IN_PROGRESS, REVIEW, DONE)
- Set task priorities (LOW, MEDIUM, HIGH, URGENT)

### Team Membership Management
- Invite members to join projects
- Accept/decline project invitations
- Leave projects
- Assign roles within projects (OWNER, ADMIN, MANAGER, DEVELOPER, QA, MEMBER)
- Transfer project ownership

### Calendar Integration
- Create project calendars
- Add events to calendars (deadlines, milestones, meetings)
- Link events to tasks
- View events by date range

### Time Tracking
- Track time spent on tasks
- Start/stop time tracking
- View time tracking records by task or user
- Add descriptions to time tracking entries

## API Endpoints

### Project Management

```
POST /api/v1/projects                 # Create a new project
GET /api/v1/projects                  # Get all projects for the authenticated user
GET /api/v1/projects/{id}             # Get a specific project by ID
PUT /api/v1/projects/{id}             # Update a project
DELETE /api/v1/projects/{id}          # Delete a project
```

### Task Management

```
POST /api/v1/projects/{projectId}/tasks                # Create a new task
GET /api/v1/projects/{projectId}/tasks                 # Get all tasks for a project
GET /api/v1/projects/{projectId}/tasks/{taskId}        # Get a specific task
PUT /api/v1/projects/{projectId}/tasks/{taskId}        # Update a task
DELETE /api/v1/projects/{projectId}/tasks/{taskId}     # Delete a task
```

### Team Membership Management

```
POST /api/v1/projects/{projectId}/members                      # Invite a member to a project
GET /api/v1/projects/{projectId}/members                       # Get all members of a project
POST /api/v1/projects/{projectId}/members/accept-invitation    # Accept an invitation to a project
DELETE /api/v1/projects/{projectId}/members/leave              # Leave a project
PUT /api/v1/projects/{projectId}/members/{userId}/role         # Update a member's role
DELETE /api/v1/projects/{projectId}/members/{userId}           # Remove a member from a project
```

### Calendar Integration

```
POST /api/v1/projects/{projectId}/calendar                     # Create a project calendar
POST /api/v1/projects/{projectId}/calendar/events              # Add an event to the calendar
GET /api/v1/projects/{projectId}/calendar/events               # Get calendar events (with optional date range)
PUT /api/v1/projects/{projectId}/calendar/events/{eventId}     # Update a calendar event
DELETE /api/v1/projects/{projectId}/calendar/events/{eventId}  # Delete a calendar event
```

### Time Tracking

```
POST /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking                     # Start time tracking for a task
PUT /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking/{timeTrackingId}     # Stop time tracking
GET /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking                      # Get time tracking records for a task
GET /api/v1/projects/{projectId}/time-tracking/my                                  # Get current user's time tracking records
DELETE /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking/{timeTrackingId}  # Delete a time tracking record
```

## Models

### Project
- id: Long
- name: String
- description: String
- ownerId: Integer
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- tasks: List<Task>
- members: List<ProjectMember>
- calendar: ProjectCalendar

### Task
- id: Long
- title: String
- description: String
- project: Project
- creatorId: Integer
- assigneeId: Integer
- status: TaskStatus (TODO, IN_PROGRESS, REVIEW, DONE)
- priority: TaskPriority (LOW, MEDIUM, HIGH, URGENT)
- deadline: LocalDateTime
- estimatedHours: Integer
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- parentTask: Task
- subtasks: Set<Task>

### ProjectMember
- id: Long
- project: Project
- userId: Integer
- role: ProjectRole (OWNER, ADMIN, MANAGER, DEVELOPER, QA, MEMBER)
- joinedAt: LocalDateTime
- invitedBy: Integer
- invitationAccepted: Boolean

### ProjectCalendar
- id: Long
- project: Project
- name: String
- description: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- events: Set<CalendarEvent>

### CalendarEvent
- id: Long
- calendar: ProjectCalendar
- title: String
- description: String
- eventType: CalendarEventType (TASK_DEADLINE, MILESTONE, SPRINT_START, SPRINT_END, MEETING, OTHER)
- startTime: LocalDateTime
- endTime: LocalDateTime
- allDay: Boolean
- location: String
- createdBy: Integer
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- task: Task (optional)

### TimeTracking
- id: Long
- task: Task
- userId: Integer
- startTime: LocalDateTime
- endTime: LocalDateTime
- durationMinutes: Integer
- description: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

## Authentication

All endpoints require an `Authorization` header with a valid JWT token in the format: `Bearer {token}`.

## Authorization

- Project owners and admins can manage project settings, members, and tasks
- Project members can view projects, create and update tasks
- Task creators and assignees can update their tasks
- Only the task creator, project owner, or admin can delete a task
- Only the project owner can transfer ownership
- Only the time tracking creator, project owner, or admin can delete time tracking records

## Error Handling

The service returns appropriate HTTP status codes and error messages:

- 200 OK: Successful operation
- 201 Created: Resource created successfully
- 204 No Content: Resource deleted successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 500 Internal Server Error: Server-side error

## Dependencies

- Spring Boot
- Spring Data JPA
- PostgreSQL
- JWT for authentication
- Lombok for reducing boilerplate code
