package com.TPM.User.Auth.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    private String password;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    @NotBlank(message = "Token is required")
    private String token;
} 