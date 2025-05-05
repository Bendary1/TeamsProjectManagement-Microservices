package com.TPM.User.Auth.user.profile;

import com.TPM.User.Auth.user.User;
import com.TPM.User.Auth.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfile getUserProfile(Integer userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user: " + userId));
    }

    @Transactional
    public UserProfile getOrCreateUserProfile(Integer userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
    }

    @Transactional
    public UserProfile updateUserProfile(Integer userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        UserProfile profile = getOrCreateUserProfile(userId);
        updateProfileFromRequest(profile, request);
        return userProfileRepository.save(profile);
    }

    @Transactional
    protected UserProfile createDefaultProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        UserProfile defaultProfile = UserProfile.builder()
                .user(user)
                .position("")
                .department("")
                .phoneNumber("")
                .timezone("UTC")
                .profileImageUrl("")
                .bio("")
                .build();

        return userProfileRepository.save(defaultProfile);
    }

    private void updateProfileFromRequest(UserProfile profile, UserProfileUpdateRequest request) {
        if (request.getPosition() != null) {
            profile.setPosition(request.getPosition());
        }
        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
    }
} 