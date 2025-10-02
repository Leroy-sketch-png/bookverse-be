package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.request.User.UpgradeToSellerRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.service.UserProfileService;
import com.example.bookverseserver.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileController {

    UserProfileService userProfileService;
    SecurityUtils securityUtils;

    @PostMapping("/me")
    public ApiResponse<ProfileResponse> createProfile(
            @Valid @RequestBody ProfileCreationRequest request,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.createProfileForUser(userId, request);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMe(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.getProfileForUser(userId);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @PutMapping("/me")
    public ApiResponse<ProfileResponse> updateMe(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.updateProfileForUser(authentication, request);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @PatchMapping("/me/avatar")
    public ApiResponse<Map<String, String>> updateAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        String avatarUrl = userProfileService.updateAvatar(userId, file);
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("avatar_url", avatarUrl))
                .build();
    }

    @PatchMapping("/me/upgrade-to-seller")
    public ApiResponse<String> upgradeToSeller(
            Authentication authentication,
            @Valid @RequestBody UpgradeToSellerRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        userProfileService.upgradeToSeller(userId, request);
        return ApiResponse.<String>builder()
                .result("Account upgraded to seller successfully.")
                .build();
    }
}
