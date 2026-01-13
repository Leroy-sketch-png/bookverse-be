package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.request.User.UpgradeToSellerRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.service.UserProfileService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Profile", description = "👤 User profile management APIs - Update profile, avatar, upgrade to seller")
public class UserProfileController {

    UserProfileService userProfileService;
    SecurityUtils securityUtils;

    @PostMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Create user profile",
        description = "Initialize profile for a new user with personal information. " +
                     "**Required after registration** to complete account setup."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Profile created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid profile data or profile already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        )
    })
    public ApiResponse<ProfileResponse> createProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Profile information (full name, phone, bio, etc.)",
                required = true,
                content = @Content(schema = @Schema(implementation = ProfileCreationRequest.class))
            )
            @Valid @RequestBody ProfileCreationRequest request,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.createProfileForUser(userId, request);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get my profile",
        description = "Retrieve current user's complete profile information including: " +
                     "- Personal details (name, email, phone) " +
                     "- Avatar URL " +
                     "- Bio and social links " +
                     "- Account role (BUYER, SELLER, ADMIN) " +
                     "- Seller verification status (if applicable)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Profile retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Profile not found"
        )
    })
    public ApiResponse<ProfileResponse> getMe(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.getProfileForUser(userId);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update my profile",
        description = "Update current user's profile information. " +
                     "Supports partial updates - only include fields you want to change. " +
                     "**Updatable fields**: Full name, phone, bio, social links, preferences"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Profile updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid update data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Profile not found"
        )
    })
    public ApiResponse<ProfileResponse> updateMe(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated profile fields",
                required = true,
                content = @Content(schema = @Schema(implementation = ProfileUpdateRequest.class))
            )
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ProfileResponse resp = userProfileService.updateProfileForUser(authentication, request);
        return ApiResponse.<ProfileResponse>builder().result(resp).build();
    }

    @PatchMapping("/me/avatar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update profile avatar",
        description = "Upload and update user profile picture. " +
                     "**File requirements**: " +
                     "- Formats: JPG, JPEG, PNG " +
                     "- Max size: 5MB " +
                     "- Recommended: Square image, min 200x200px " +
                     "Image will be stored in cloud storage and URL returned."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Avatar updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid file format or size exceeds limit"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "File upload failed"
        )
    })
    public ApiResponse<Map<String, String>> updateAvatar(
            Authentication authentication,
            
            @Parameter(
                description = "Avatar image file (JPG, PNG, max 5MB)",
                required = true,
                content = @Content(mediaType = "multipart/form-data")
            )
            @RequestParam("file") MultipartFile file
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        String avatarUrl = userProfileService.updateAvatar(userId, file);
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("avatar_url", avatarUrl))
                .build();
    }

    @PatchMapping("/me/upgrade-to-seller")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Upgrade account to seller",
        description = "Request to upgrade from BUYER to SELLER role. " +
                     "**Requirements**: " +
                     "- Must provide business/shop name " +
                     "- Must provide valid address " +
                     "- May require admin approval (depending on config) " +
                     "**After approval**: User can create and manage book listings"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Account upgraded successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid request or already a seller"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "User already has SELLER role"
        )
    })
    public ApiResponse<String> upgradeToSeller(
            Authentication authentication,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Seller information (business name, address, tax ID, etc.)",
                required = true,
                content = @Content(schema = @Schema(implementation = UpgradeToSellerRequest.class))
            )
            @Valid @RequestBody UpgradeToSellerRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        userProfileService.upgradeToSeller(userId, request);
        return ApiResponse.<String>builder()
                .result("Account upgraded to seller successfully.")
                .build();
    }
}
