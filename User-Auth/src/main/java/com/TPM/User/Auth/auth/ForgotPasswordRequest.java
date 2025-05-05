package com.TPM.User.Auth.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForgotPasswordRequest {
    @Email(message = "Email is not valid")
    @NotBlank(message = "Email is required")
    private String email;
} 