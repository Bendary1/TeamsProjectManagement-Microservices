# Teams Project Management System API Documentation

This document provides a comprehensive guide to the TeamsProjectManagement system API, including all endpoints, authorization mechanisms, request/response structures, and information required for building the frontend with React.

## Table of Contents

1. [Authentication](#authentication)
2. [Projects](#projects)
3. [Tasks](#tasks)
4. [Project Members](#project-members)
5. [Time Tracking](#time-tracking)
6. [Calendar](#calendar)
7. [User Profiles](#user-profiles)
8. [Authorization](#authorization)
9. [Error Handling](#error-handling)
10. [Frontend Development Guide](#frontend-development-guide)

## System Architecture

The TeamsProjectManagement system follows a microservices architecture with the following components:

- **User Authentication Service** - User management and authentication (Port: 8888 with context path /api/v1/)
- **Project Management Service** - Project and task management (Port: 8899 with context path /api/v1/)

## Authentication

### Base URL: `/api/v1/auth`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/register` | POST | Register a new user | No |
| `/authenticate` | POST | Authenticate a user | No |
| `/activate-account` | GET | Activate a user account | No |
| `/forgot-password` | POST | Request password reset | No |
| `/reset-password` | POST | Reset password | No |

### Request/Response Examples

#### Register User

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "secure_password"
}
```

**Response:**
- HTTP Status: 202 Accepted
- No response body
- An email will be sent for account activation

#### Authentication

**Request:**
```json
{
  "email": "john.doe@example.com",
  "password": "secure_password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 123,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

#### Forgot Password

**Request:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response:**
- HTTP Status: 200 OK
- No response body
- An email will be sent with reset instructions

#### Reset Password

**Request:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "new_secure_password"
}
```

**Response:**
- HTTP Status: 200 OK
- No response body

## Projects

### Base URL: `/api/v1/projects`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/` | POST | Create a new project | Yes |
| `/` | GET | Get all projects for user | Yes |
| `/{id}` | GET | Get project by ID | Yes |
| `/{id}` | PUT | Update project | Yes |
| `/{id}` | DELETE | Delete project | Yes |

### Request/Response Examples

#### Create Project

**Request:**
```json
{
  "name": "Project Name",
  "description": "Project Description",
  "memberIds": [1, 2, 3],
  "adminIds": [4, 5]
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Project Name",
  "description": "Project Description",
  "ownerId": 1,
  "createdAt": "2023-06-15T10:30:00",
  "updatedAt": "2023-06-15T10:30:00",
  "memberIds": [1, 2, 3],
  "adminIds": [4, 5]
}
```

## Tasks

### Base URL: `/api/v1/projects/{projectId}/tasks`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/` | POST | Create a new task | Yes |
| `/` | GET | Get all tasks for project | Yes |
| `/{taskId}` | GET | Get task by ID | Yes |
| `/{taskId}` | PUT | Update task | Yes |
| `/{taskId}` | DELETE | Delete task | Yes |
| `/{taskId}/ai-plan` | GET | Generate AI task plan | Yes |

### Request/Response Examples

#### Create Task

**Request:**
```json
{
  "title": "Task Title",
  "description": "Task Description",
  "priority": "HIGH",
  "status": "TODO",
  "dueDate": "2023-07-15T10:30:00",
  "assigneeId": 2
}
```

**Response:**
```json
{
  "id": 1,
  "projectId": 1,
  "title": "Task Title",
  "description": "Task Description",
  "priority": "HIGH",
  "status": "TODO",
  "createdAt": "2023-06-15T10:30:00",
  "updatedAt": "2023-06-15T10:30:00",
  "dueDate": "2023-07-15T10:30:00",
  "createdBy": 1,
  "assigneeId": 2
}
```

## Project Members

### Base URL: `/api/v1/projects/{projectId}/members`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/` | POST | Invite member to project | Yes |
| `/` | GET | Get project members | Yes |
| `/accept-invitation` | POST | Accept project invitation | Yes |
| `/leave` | DELETE | Leave project | Yes |
| `/{userId}/role` | PUT | Update member role | Yes |
| `/{userId}` | DELETE | Remove member from project | Yes |

### Request/Response Examples

#### Invite Member

**Request:**
```json
{
  "userId": 123,
  "role": "MEMBER"
}
```

**Response:**
```json
{
  "id": 1,
  "projectId": 1,
  "userId": 123,
  "role": "MEMBER",
  "status": "PENDING",
  "invitedAt": "2023-06-15T10:30:00",
  "joinedAt": null
}
```

## Time Tracking

### Base URL: `/api/v1/projects/{projectId}`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/tasks/{taskId}/time-tracking` | POST | Start time tracking | Yes |
| `/tasks/{taskId}/time-tracking/{timeTrackingId}` | PUT | Stop time tracking | Yes |
| `/tasks/{taskId}/time-tracking` | GET | Get time trackings for task | Yes |
| `/time-tracking/my` | GET | Get user's time trackings for project | Yes |
| `/tasks/{taskId}/time-tracking/{timeTrackingId}` | DELETE | Delete time tracking | Yes |

### Request/Response Examples

#### Start Time Tracking

**Request:**
```json
{
  "description": "Working on feature X",
  "startTime": "2023-06-15T10:30:00"
}
```

**Response:**
```json
{
  "id": 1,
  "taskId": 1,
  "userId": 123,
  "description": "Working on feature X",
  "startTime": "2023-06-15T10:30:00",
  "endTime": null,
  "duration": null
}
```

## Calendar

### Base URL: `/api/v1/projects/{projectId}/calendar`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/` | POST | Create project calendar | Yes |
| `/events` | POST | Add calendar event | Yes |
| `/events` | GET | Get calendar events | Yes |
| `/events/{eventId}` | PUT | Update calendar event | Yes |
| `/events/{eventId}` | DELETE | Delete calendar event | Yes |

### Request/Response Examples

#### Add Calendar Event

**Request:**
```json
{
  "title": "Sprint Planning",
  "description": "Plan tasks for next sprint",
  "startTime": "2023-06-20T10:00:00",
  "endTime": "2023-06-20T11:30:00",
  "allDay": false,
  "location": "Conference Room A",
  "attendeeIds": [1, 2, 3, 4]
}
```

**Response:**
```json
{
  "id": 1,
  "calendarId": 1,
  "title": "Sprint Planning",
  "description": "Plan tasks for next sprint",
  "startTime": "2023-06-20T10:00:00",
  "endTime": "2023-06-20T11:30:00",
  "allDay": false,
  "location": "Conference Room A",
  "createdBy": 1,
  "attendees": [
    {
      "userId": 1,
      "status": "ACCEPTED"
    },
    {
      "userId": 2,
      "status": "PENDING"
    },
    {
      "userId": 3,
      "status": "PENDING"
    },
    {
      "userId": 4,
      "status": "PENDING"
    }
  ]
}
```

## User Profiles

### Base URL: `/api/v1/auth/users/me/profile`

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/` | GET | Get current user profile | Yes |
| `/` | PUT | Update current user profile | Yes |

### Request/Response Examples

#### Get User Profile

**Response:**
```json
{
  "id": 1,
  "userId": 123,
  "bio": "Software Engineer with 5 years of experience",
  "jobTitle": "Senior Developer",
  "company": "Tech Company",
  "location": "New York",
  "phone": "+1234567890",
  "profileImageUrl": "https://example.com/profile.jpg",
  "skills": ["Java", "Spring Boot", "React"]
}
```

#### Update User Profile

**Request:**
```json
{
  "bio": "Updated bio information",
  "jobTitle": "Lead Developer",
  "company": "Tech Company",
  "location": "San Francisco",
  "phone": "+1987654321",
  "skills": ["Java", "Spring Boot", "React", "Microservices"]
}
```

**Response:**
```json
{
  "id": 1,
  "userId": 123,
  "bio": "Updated bio information",
  "jobTitle": "Lead Developer",
  "company": "Tech Company",
  "location": "San Francisco",
  "phone": "+1987654321",
  "profileImageUrl": "https://example.com/profile.jpg",
  "skills": ["Java", "Spring Boot", "React", "Microservices"]
}
```

## Authorization

The system uses JWT (JSON Web Token) based authentication:

1. **Token Format**: `Bearer <JWT_TOKEN>`
2. **Token Inclusion**: Include the token in the `Authorization` header for all authenticated requests
3. **Token Expiration**: Tokens expire after a certain period (typically 24 hours)
4. **Token Refresh**: The frontend should handle token refresh before expiration

### Project Access Levels

- **OWNER**: Has full control over the project, can delete project, add/remove members, and change roles
- **ADMIN**: Can manage tasks, members, and settings, but cannot delete the project
- **MEMBER**: Can view project details and work on tasks
- **GUEST**: Limited access to view specific project information (if supported)

## Error Handling

The API returns standard HTTP status codes and error responses:

- **400 Bad Request**: Invalid input data
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

### Error Response Format

```json
{
  "status": 404,
  "message": "Project not found with ID: 123",
  "timestamp": "2023-06-15T10:30:00"
}
```

## Frontend Development Guide

### Getting Started

1. **Authentication Flow**:
   - Implement registration and login forms
   - Store JWT token in secure storage (e.g., localStorage or cookies)
   - Include token in all API requests using an axios interceptor or similar

2. **API Base URLs**:
   - Use environment variables for base URLs
   - Set up a proxy in development to avoid CORS issues

3. **State Management**:
   - Consider Redux, Context API, or other state management libraries
   - Implement proper caching strategies for project and task data

### UI Components Needed

1. **Authentication**:
   - Login form
   - Registration form
   - Password reset form

2. **Project Management**:
   - Project list view
   - Project detail view
   - Project creation/edit form
   - Member management interface

3. **Task Management**:
   - Task board (kanban style)
   - Task list view
   - Task detail view
   - Task creation/edit form

4. **Time Tracking**:
   - Timer component
   - Time logs view
   - Reports and summaries

5. **Calendar**:
   - Month/week/day views
   - Event creation interface
   - Event detail view

### Recommended Libraries

1. **UI Framework**:
   - Material-UI or Ant Design for comprehensive component libraries

2. **Routing**:
   - React Router for navigation

3. **Forms**:
   - Formik or React Hook Form for form management
   - Yup for validation

4. **Date/Time Handling**:
   - date-fns or moment.js for date manipulation
   - react-big-calendar for calendar views

5. **API Communication**:
   - axios or fetch for API calls
   - React Query for data fetching, caching, and state management

6. **Data Visualization**:
   - recharts or Chart.js for time tracking reports

### Authentication Integration

```javascript
// Example axios setup with auth interceptor
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Handle unauthorized error (redirect to login)
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Project Structure Example

```
src/
├── api/
│   ├── auth.js
│   ├── projects.js
│   ├── tasks.js
│   └── timeTracking.js
├── components/
│   ├── auth/
│   ├── layout/
│   ├── projects/
│   ├── tasks/
│   └── timeTracking/
├── contexts/
│   ├── AuthContext.js
│   └── ProjectContext.js
├── hooks/
│   ├── useAuth.js
│   ├── useProjects.js
│   └── useTasks.js
├── pages/
│   ├── Auth/
│   ├── Dashboard/
│   ├── Projects/
│   └── Tasks/
├── utils/
│   ├── formatters.js
│   └── validators.js
└── App.js
```

### Responsive Design Considerations

The frontend should be designed with a mobile-first approach, ensuring all features are accessible on various device sizes:

1. **Responsive Layouts**:
   - Use CSS Grid and Flexbox for flexible layouts
   - Implement breakpoints for different device sizes

2. **Touch-Friendly UI**:
   - Ensure buttons and interactive elements are large enough for touch interaction
   - Implement touch gestures where appropriate

3. **Performance Optimization**:
   - Lazy load components and routes
   - Optimize images and assets
   - Implement code splitting

### Error Handling Strategy

1. **Global Error Boundary**:
   - Implement React Error Boundaries to catch UI rendering errors

2. **API Error Handling**:
   - Create consistent error display components
   - Implement retry mechanisms for failed requests

3. **Form Validation**:
   - Provide clear, user-friendly validation messages
   - Implement both client-side and server-side validation 