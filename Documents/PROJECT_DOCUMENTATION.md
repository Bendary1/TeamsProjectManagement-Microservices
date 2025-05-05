# Teams Project Management System - Microservices Architecture

## Overview

The Teams Project Management System is a microservices-based application designed to facilitate project management, team collaboration, and task tracking. The system follows a modern microservices architecture, with separate services handling different aspects of the application, all communicating through RESTful APIs.

## System Architecture

The application is built using a microservices architecture with the following components:

### Core Infrastructure Services

1. **Discovery Service** (Eureka)
   - Port: 8761
   - Responsible for service registration and discovery
   - Allows services to find and communicate with each other without hardcoded URLs

2. **Config Server**
   - Port: 8888
   - Centralized configuration management
   - Provides configuration properties to all services

3. **API Gateway**
   - Routes client requests to appropriate services
   - Handles cross-cutting concerns like authentication, logging, etc.

### Business Services

1. **User Authentication Service**
   - Port: 8888 (with context path /api/v1/)
   - Handles user registration, authentication, and authorization
   - Manages user profiles and roles
   - JWT-based authentication
   - User IDs are of type Integer

2. **Project Management Service**
   - Port: 8899 (with context path /api/v1/)
   - Manages projects, including creation, updates, and deletion
   - Handles project membership and permissions
   - Manages tasks, time tracking, and project members
   - References User IDs as Integer

## Database Architecture

Each service has its own dedicated database:

1. **User Authentication Database**
   - PostgreSQL (port 5544)
   - Database name: users_auth_db
   - Contains user accounts, profiles, roles, and permissions

2. **Project Management Database**
   - PostgreSQL (port 5555)
   - Database name: project_management_db
   - Stores project data, memberships, tasks, time tracking, and related information

## Service Details

### 1. Discovery Service (Eureka)

The Discovery Service uses Netflix Eureka for service registration and discovery. All microservices register themselves with the Eureka server, allowing them to discover and communicate with each other dynamically.

**Key Features:**
- Service registration
- Health monitoring
- Load balancing support

### 2. Config Server

The Config Server provides centralized configuration management for all services. It can pull configuration from a Git repository or local files.

**Key Features:**
- Environment-specific configurations
- Runtime configuration updates
- Encrypted property support

### 3. API Gateway

The API Gateway serves as the entry point for all client requests. It routes requests to the appropriate services and handles cross-cutting concerns.

**Key Features:**
- Request routing
- Load balancing
- Security filters
- Rate limiting

### 4. User Authentication Service

The User Authentication Service handles all aspects of user management, authentication, and authorization.

**Key Features:**

#### Authentication
- User registration with email verification
- JWT-based authentication
- Password management (reset, change)
- Account activation

#### User Management
- User profiles with professional information
- Role-based access control
- Permission management

#### API Endpoints

**Authentication:**
```
POST /api/v1/auth/register
POST /api/v1/auth/authenticate
GET /api/v1/auth/activate-account
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

**User Management:**
```
GET /api/v1/auth/users/me
PUT /api/v1/auth/users/me
GET /api/v1/auth/users/me/profile
PUT /api/v1/auth/users/me/profile
```

**Role Management:**
```
POST /api/v1/auth/roles/assign
GET /api/v1/auth/users/{userId}/roles
DELETE /api/v1/auth/users/{userId}/roles/{roleId}
```

#### Domain Model

**User Entity:**
- Basic information (name, email, password)
- Account status (enabled, locked)
- Roles and permissions
- Profile information

**Role Entity:**
- Name
- Associated permissions
- User assignments

### 5. Project Management Service

The Project Management Service handles all project-related operations, including creation, updates, team management, task management, and time tracking.

**Key Features:**
- Project creation and management
- Team member assignment and invitation management
- Project ownership and permissions
- Task creation, assignment, and tracking
- Time tracking for tasks

#### API Endpoints

**Project Management:**
```
POST /api/v1/projects                 # Create a new project
GET /api/v1/projects                  # Get all projects for the authenticated user
GET /api/v1/projects/{id}             # Get a specific project by ID
PUT /api/v1/projects/{id}             # Update a project
DELETE /api/v1/projects/{id}          # Delete a project
GET /api/v1/test                      # Test endpoint to verify service is running
```

**Task Management:**
```
POST /api/v1/projects/{projectId}/tasks               # Create a new task
GET /api/v1/projects/{projectId}/tasks                # Get all tasks for project
GET /api/v1/projects/{projectId}/tasks/{taskId}       # Get task by ID
PUT /api/v1/projects/{projectId}/tasks/{taskId}       # Update task
DELETE /api/v1/projects/{projectId}/tasks/{taskId}    # Delete task
GET /api/v1/projects/{projectId}/tasks/{taskId}/ai-plan # Generate AI task plan
```

**Project Members:**
```
POST /api/v1/projects/{projectId}/members             # Invite member to project
GET /api/v1/projects/{projectId}/members              # Get project members
POST /api/v1/projects/{projectId}/members/accept-invitation # Accept project invitation
DELETE /api/v1/projects/{projectId}/members/leave     # Leave project
PUT /api/v1/projects/{projectId}/members/{userId}/role # Update member role
DELETE /api/v1/projects/{projectId}/members/{userId}  # Remove member from project
```

**Time Tracking:**
```
POST /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking         # Start time tracking
PUT /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking/{timeTrackingId} # Stop time tracking
GET /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking          # Get time trackings for task
GET /api/v1/projects/{projectId}/time-tracking/my                      # Get user's time trackings for project
DELETE /api/v1/projects/{projectId}/tasks/{taskId}/time-tracking/{timeTrackingId} # Delete time tracking
```

**Request/Response Examples:**

Create Project Request:
```json
{
  "name": "Project Name",
  "description": "Project Description",
  "memberIds": [1, 2, 3],
  "adminIds": [4, 5]
}
```

Project Response:
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

Create Task Request:
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

Task Response:
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

Invite Member Request:
```json
{
  "userId": 123,
  "role": "MEMBER"
}
```

Member Response:
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

Start Time Tracking Request:
```json
{
  "description": "Working on feature X",
  "startTime": "2023-06-15T10:30:00"
}
```

**Note:** All project endpoints require an `Authorization` header with a valid JWT token in the format: `Bearer {token}`.

#### Domain Model

**Project Entity:**
- Basic information (name, description)
- Owner ID
- Creation and update timestamps
- Member IDs
- Admin IDs

**Task Entity:**
- Basic information (title, description)
- Project ID
- Priority and status
- Due date
- Assignee ID
- Creator ID
- Creation and update timestamps

**Project Member Entity:**
- Project ID
- User ID
- Role (OWNER, ADMIN, MEMBER)
- Status (PENDING, ACTIVE)
- Invited At and Joined At timestamps

**Time Tracking Entity:**
- Task ID
- User ID
- Start time and end time
- Description
- Duration

## Security Implementation

The system implements a comprehensive security model:

1. **Authentication:**
   - JWT-based token authentication
   - Secure password storage with encryption
   - Account activation via email

2. **Authorization:**
   - Role-based access control
   - Fine-grained permissions
   - Resource-level authorization checks

3. **API Security:**
   - HTTPS support
   - CORS configuration
   - Input validation
   - Rate limiting

## Communication Patterns

The microservices communicate with each other using the following patterns:

1. **Synchronous Communication:**
   - REST APIs with Feign clients
   - Service discovery via Eureka

2. **Service-to-Service Authentication:**
   - JWT token propagation
   - Client credentials

## Deployment

The application is containerized using Docker, with each service having its own container. Docker Compose is used for local development and testing.

**Docker Compose Configuration:**
- PostgreSQL databases
- Service containers
- Network configuration
- Volume management
- Zipkin for distributed tracing

## Development Tools and Technologies

### Core Technologies
- Java 17
- Spring Boot 3.4.x
- Spring Cloud (Eureka, Config Server, Gateway)
- Spring Data JPA
- Spring Security
- PostgreSQL

### Build Tools
- Maven

### Testing
- JUnit
- Spring Boot Test

### Documentation
- SpringDoc (OpenAPI)

### Monitoring and Observability
- Zipkin for distributed tracing

## Frontend Development

The frontend is developed using React, providing a modern and responsive user interface for the application. The frontend communicates with the backend services through the API Gateway.

### Key Frontend Features
- User authentication and profile management
- Project creation and management
- Task management with drag-and-drop functionality
- Time tracking with reports
- Team management
- Responsive design for mobile and desktop

## Known Issues and Limitations

1. **Type Mismatch Between Services** (FIXED)
   - The User Auth service uses `Integer` for User IDs
   - The Project Management service was referencing User IDs as `Long`
   - This was causing issues when the Project Management service tried to communicate with the User Auth service
   - **Fix Applied**: Updated the following classes to use `Integer` instead of `Long` for user IDs:
     - `UserProfileResponse`
     - `Project` entity
     - `ProjectRequest` and `ProjectResponse` DTOs
     - `ProjectRepository` methods
     - `ProjectService` methods

2. **Feign Client Configuration**
   - The Project Management service uses Feign to communicate with the User Auth service
   - The error handling is implemented, but there might be issues with response mapping
   - **Fix**: Ensure that the response DTOs in the Project Management service match the actual response structure from the User Auth service

3. **Security Configuration**
   - The Project Management service excludes Spring Security auto-configuration
   - This might lead to security vulnerabilities if not properly addressed
   - **Fix**: Implement proper security configuration in the Project Management service

## Getting Started

### Prerequisites
- Java 17
- Docker and Docker Compose
- Maven
- Node.js and npm (for frontend development)

### Running the Application

1. Clone the repository
2. Start the infrastructure services:
   ```
   cd discovery
   ./mvnw spring-boot:run

   cd ../config-server
   ./mvnw spring-boot:run

   cd ../gateway
   ./mvnw spring-boot:run
   ```

3. Start the databases:
   ```
   cd User-Auth
   docker-compose up -d postgres

   cd ../project-management-service
   docker-compose up -d postgres
   ```

4. Start the business services:
   ```
   cd ../User-Auth
   ./mvnw spring-boot:run

   cd ../project-management-service
   ./mvnw spring-boot:run
   ```

5. Start the frontend application:
   ```
   cd Teams\ project\ Management\ Frontend
   npm install
   npm start
   ```

6. Access the services:
   - Eureka Dashboard: http://localhost:8761
   - User Auth API: http://localhost:8888/api/v1/
   - Project Management API: http://localhost:8899/api/v1/
   - Frontend Application: http://localhost:3000

## Changes Made to Fix Issues

The following changes were made to fix issues in the project:

1. **Fixed Type Mismatch Between Services**
   - Changed user ID types from `Long` to `Integer` in the Project Management service to match the User Auth service
   - Updated the following files:
     - `UserProfileResponse.java`: Changed ID fields from Long to Integer
     - `Project.java`: Changed ownerId and collection types from Long to Integer
     - `ProjectRequest.java`: Changed collection types from Long to Integer
     - `ProjectResponse.java`: Changed ownerId and collection types from Long to Integer
     - `ProjectRepository.java`: Updated method signatures to use Integer
     - `ProjectService.java`: Updated method parameters and loops to use Integer
     - `UserServiceClient.java`: Updated method parameter type

2. **Improved Documentation**
   - Added detailed API endpoint descriptions
   - Added request/response examples
   - Documented known issues and their fixes
   - Added more detailed instructions for running the application

## Conclusion

The Teams Project Management System provides a robust, scalable platform for managing projects and teams. Its microservices architecture allows for independent development, deployment, and scaling of each component, making it suitable for organizations of various sizes. The integration of task management, time tracking, and team collaboration features creates a comprehensive project management solution.
