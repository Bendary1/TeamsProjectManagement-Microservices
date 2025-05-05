package com.TPM.User.Auth.user.profile;

import com.TPM.User.Auth.user.User;
import com.TPM.User.Auth.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/users/me/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfile> getCurrentUserProfile(@AuthenticationPrincipal User user) {
        UserProfile profile = userProfileService.getOrCreateUserProfile(user.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfile> updateCurrentUserProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfile updatedProfile = userProfileService.updateUserProfile(user.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }
} 