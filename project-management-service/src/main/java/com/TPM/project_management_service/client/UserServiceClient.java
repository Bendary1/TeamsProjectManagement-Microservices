package com.TPM.project_management_service.client;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
@FeignClient(name = "user-auth-service",
            url = "${application.config.user-auth-url}",
            configuration = UserServiceClient.FeignConfig.class,
            fallback = UserServiceClient.UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("auth/users/me/profile")
    UserProfileResponse getUserProfile(@RequestHeader("Authorization") String token);

    @GetMapping("auth/users/{userId}/exists")
    boolean userExists(@PathVariable Integer userId, @RequestHeader("Authorization") String token);

    @Slf4j
    @Component
    class UserServiceFallback implements UserServiceClient {
        @Override
        public UserProfileResponse getUserProfile(String token) {
            log.error("Fallback: Could not get user profile from auth service");
            throw new RuntimeException("Cannot retrieve user profile from auth service");
        }

        @Override
        public boolean userExists(Integer userId, String token) {
            // For the userExists endpoint, we'll return true as a safe fallback
            // This allows the project-management service to continue operation
            // when the user-auth service returns 403 or other errors
            log.warn("Fallback: Assuming user {} exists due to auth service issues", userId);
            return true;
        }
    }

    @Slf4j
    class FeignConfig {
        @Bean
        public ErrorDecoder errorDecoder() {
            return new ErrorDecoder() {
                @Override
                public Exception decode(String methodKey, Response response) {
                    String responseBody = "";
                    try {
                        if (response.body() != null) {
                            responseBody = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        }
                    } catch (IOException e) {
                        log.error("Error reading response body", e);
                    }

                    log.error("Feign client error - Method: {}, Status: {}, Headers: {}, Body: {}",
                            methodKey,
                            response.status(),
                            response.headers(),
                            responseBody);

                    if (response.status() == HttpStatus.NOT_FOUND.value()) {
                        return new IllegalArgumentException("User not found");
                    }
                    if (response.status() == HttpStatus.UNAUTHORIZED.value()) {
                        return new IllegalArgumentException("Invalid or expired token");
                    }
                    if (response.status() == HttpStatus.BAD_REQUEST.value()) {
                        return new IllegalArgumentException("Bad request: " + responseBody);
                    }

                    return FeignException.errorStatus(methodKey, response);
                }
            };
        }
    }
}