package com.TPM.User.Auth.user.profile;

import lombok.Data;

import java.util.Set;

@Data
public class UserProfileUpdateRequest {
    private String position;
    private String department;
    private String phoneNumber;
    private String timezone;
    private Set<String> skills;
    private String profileImageUrl;
    private String bio;
} 