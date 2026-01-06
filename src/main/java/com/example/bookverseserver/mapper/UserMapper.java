package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.User.UserProfileSummary;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User mapper aligned with Vision API_CONTRACTS.md.
 * Produces nested profile structure required by frontend.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    /**
     * Maps User entity to Vision-compliant UserResponse.
     * Includes nested profile, role names as strings, accountType, and isVerified.
     */
    default UserResponse toUserResponse(User user) {
        if (user == null) return null;

        // Build nested profile summary
        UserProfileSummary profileSummary = null;
        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            profileSummary = UserProfileSummary.builder()
                    .displayName(profile.getDisplayName())
                    .phone(profile.getPhoneNumber())
                    .avatar(profile.getAvatarUrl())
                    .bio(profile.getBio())
                    .location(profile.getLocation())
                    .joinedAt(profile.getCreatedAt() != null 
                            ? profile.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) 
                            : null)
                    .build();
        }

        // Map roles to Set<String> (just role names)
        Set<String> roleNames = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleNames = user.getRoles().stream()
                    .map(role -> role.getName().name())  // RoleName enum to String
                    .collect(Collectors.toSet());
        }

        // Determine account type from profile or default
        String accountType = "BUYER";
        if (profile != null && profile.getAccountType() != null) {
            accountType = profile.getAccountType();
        }

        // Build the response
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profile(profileSummary)
                .roles(roleNames)
                .accountType(accountType)
                .isVerified(user.getEnabled())  // enabled = email verified
                .build();
    }

    @Mapping(target = "roles", ignore = true) // Roles handled manually in service
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
