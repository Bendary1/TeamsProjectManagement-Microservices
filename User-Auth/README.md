# User-Auth Service Documentation

## Overview
The User-Auth service is a core component of the Project Management System, handling user authentication, authorization, and profile management. This service provides secure access control and user management capabilities for the entire system.

## Features

### 1. Authentication
- **User Registration**
  - Email-based registration
  - Account activation via email
  - Password validation and encryption
  - First-time user setup

- **Login System**
  - JWT-based authentication
  - Session management
  - Remember me functionality
  - Multi-device support

- **Password Management**
  - Secure password reset
  - Password strength validation
  - Password history tracking
  - Account lockout after failed attempts

### 2. Authorization
- **Role-Based Access Control (RBAC)**
  - Predefined roles:
    - PROJECT_MANAGER
    - TEAM_LEADER
    - TEAM_MEMBER
    - ADMIN
    - STAKEHOLDER
    - GUEST
  - Role assignment and management
  - Role hierarchy

- **Permission System**
  - Granular permissions:
    - CREATE_PROJECT
    - EDIT_PROJECT
    - DELETE_PROJECT
    - ASSIGN_TASKS
    - MANAGE_TEAM
    - VIEW_REPORTS
    - MANAGE_SETTINGS
  - Permission-based access control
  - Permission inheritance

### 3. User Profile Management
- **Enhanced User Profile**
  - Professional information
    - Position
    - Department
    - Contact details
  - Personal preferences
    - Time zone
    - Language
    - Notification settings
  - Skills and expertise
  - Profile image management

- **Team Integration**
  - Team membership
  - Team roles
  - Team-based permissions
  - Team switching capability

### 4. Security Features
- **Two-Factor Authentication (2FA)**
  - TOTP-based 2FA
  - Backup codes
  - Recovery options
  - Device management

- **Session Management**
  - Active session tracking
  - Session timeout
  - Concurrent session control
  - Remote session termination

- **Activity Logging**
  - Authentication events
  - Security-related actions
  - Profile changes
  - Access attempts

### 5. Organization Management
- **Organization Structure**
  - Organization creation and management
  - Department hierarchy
  - Team organization
  - Member management

- **Organization Settings**
  - Security policies
  - User management policies
  - Custom roles and permissions
  - Integration settings

## API Endpoints

### Authentication
```
POST /api/v1/auth/register
POST /api/v1/auth/authenticate
GET /api/v1/auth/activate-account
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

### User Management
```
GET /api/v1/auth/users/me
PUT /api/v1/auth/users/me
GET /api/v1/auth/users/me/profile
PUT /api/v1/auth/users/me/profile
```

### Role Management
```
POST /api/v1/auth/roles/assign
GET /api/v1/auth/users/{userId}/roles
DELETE /api/v1/auth/users/{userId}/roles/{roleId}
```

### Team Management
```
POST /api/v1/auth/teams/{teamId}/members
GET /api/v1/auth/teams/{teamId}/members
GET /api/v1/auth/users/me/teams
```

### Organization Management
```
POST /api/v1/auth/organizations
GET /api/v1/auth/organizations
PUT /api/v1/auth/organizations/{orgId}
POST /api/v1/auth/organizations/{orgId}/members
```

### Security
```
POST /api/v1/auth/2fa/enable
POST /api/v1/auth/2fa/verify
POST /api/v1/auth/2fa/disable
GET /api/v1/auth/2fa/backup-codes
GET /api/v1/auth/sessions
POST /api/v1/auth/sessions/revoke
POST /api/v1/auth/sessions/revoke-all
```

## Database Schema

### User Entity
```java
@Entity
public class User {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private boolean enabled;
    private boolean accountLocked;
    private Set<Role> roles;
    private UserProfile profile;
    private Set<Team> teams;
    private Set<Permission> permissions;
    private boolean twoFactorEnabled;
    private List<AuthActivity> activities;
    private Organization organization;
}
```

### Role Entity
```java
@Entity
public class Role {
    private Long id;
    private String name;
    private Set<Permission> permissions;
    private Set<User> users;
}
```

### Team Entity
```java
@Entity
public class Team {
    private Long id;
    private String name;
    private Set<User> members;
    private Organization organization;
}
```

## Security Configuration

### JWT Configuration
```yaml
application:
  security:
    jwt:
      secret-key: your-secret-key
      expiration: 86400000
```

### 2FA Configuration
```yaml
application:
  security:
    2fa:
      enabled: true
      issuer: "Project Management System"
```

### Session Configuration
```yaml
application:
  security:
    session:
      max-concurrent: 5
      max-age-days: 30
```

## Integration Points

### 1. Service Discovery
- Registered with Eureka
- Health check endpoints
- Service metadata

### 2. API Gateway
- Route configuration
- Security filters
- Rate limiting

### 3. Config Server
- Centralized configuration
- Environment-specific settings
- Secure property encryption

## Monitoring and Logging

### 1. Health Checks
- Database connectivity
- External service dependencies
- Resource utilization

### 2. Metrics
- Authentication attempts
- Session statistics
- Error rates
- Response times

### 3. Logging
- Security events
- User actions
- System errors
- Performance metrics

## Best Practices

### 1. Security
- Always use HTTPS
- Implement proper CORS
- Validate all inputs
- Use secure password hashing
- Implement proper session management

### 2. Performance
- Use caching where appropriate
- Optimize database queries
- Implement proper indexing
- Use connection pooling

### 3. Scalability
- Stateless design
- Horizontal scaling support
- Load balancing
- Caching strategies

## Future Enhancements

1. **Social Authentication**
   - Google OAuth
   - GitHub OAuth
   - Microsoft OAuth

2. **Advanced Analytics**
   - User behavior tracking
   - Security threat detection
   - Usage patterns

3. **Enhanced Security**
   - Biometric authentication
   - Hardware token support
   - Advanced threat detection

4. **Integration Features**
   - Single Sign-On (SSO)
   - LDAP integration
   - Active Directory integration 