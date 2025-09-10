package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.service.UserProfileService;
import com.example.bookverseserver.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileController {

    UserProfileService userProfileService;
    SecurityUtils securityUtils;

    @PostMapping("/profile")
    public ApiResponse<ProfileResponse> createProfile(
            @Valid @RequestBody ProfileCreationRequest request,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.createProfileForUser(userId, request);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getMyProfile(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.getProfileForUser(userId);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<ProfileResponse> getProfileByUserId(@PathVariable Long userId) {
        ProfileResponse resp = userProfileService.getProfileByUserId(userId);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @PutMapping("/profile")
    public ApiResponse<ProfileResponse> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.updateProfileForUser(userId, request);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @DeleteMapping("/profile")
    public ApiResponse<String> deleteMyProfile(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        userProfileService.deleteProfileForUser(userId);
        return ApiResponse.<String>builder().result("Profile deleted").build();
    }
}
