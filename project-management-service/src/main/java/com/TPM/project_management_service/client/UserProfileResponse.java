package com.TPM.project_management_service.client;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponse {
    private Integer id;
    private User user;
    private String position;
    private String department;
    private String phoneNumber;
    private String timezone;
    private List<String> skills;
    private String profileImageUrl;
    private String bio;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @Data
    public static class User {
        private Integer id;
        private String firstname;
        private String lastname;
        private String dateOfBirth;
        private String email;
        private String password;
        private boolean accountLocked;
        private boolean enabled;
        private List<Role> roles;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private String name;
        private String username;
        private List<Authority> authorities;
        private boolean credentialsNonExpired;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
    }

    @Data
    public static class Role {
        private Integer id;
        private String name;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
    }

    @Data
    public static class Authority {
        private String authority;
    }
}