package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    /**
     * From creation request to entity.
     */
    UserProfile toEntity(ProfileCreationRequest request);

    /**
     * From entity to response.
     * Need to map user.id -> userId explicitly, rest is auto-mapped by name.
     */
    @Mapping(source = "user.id", target = "userId")
    ProfileResponse toResponse(UserProfile profile);

    /**
     * Partial update: ignore nulls in DTO. Only properties present in DTO
     * will be mapped. avatarUrl stays out of the flow.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "avatarUrl", ignore = true)
    void updateFromDto(ProfileUpdateRequest dto, @MappingTarget UserProfile profile);

//    default UserProfile toEntity(ProfileCreationRequest request) {
//        if (request == null) return null;
//
//        return UserProfile.builder()
//                .displayName(request.getDisplayName())
//                .fullName(request.getFullName())
//                .phoneNumber(request.getPhoneNumber())
//                .bio(request.getBio())
//                .accountType(request.getAccountType())
//                .location(request.getLocation())
//                .preferences(request.getPreferences())
//                .build();
//    }
//default ProfileResponse toResponse(UserProfile profile) {
//    if (profile == null) {
//        return null;
//    }
//
//    ProfileResponse dto = new ProfileResponse();
//    dto.setId(profile.getId());
//    dto.setUserId(profile.getUser() != null ? profile.getUser().getId() : null);
//    dto.setDisplayName(profile.getDisplayName());
//    dto.setFullName(profile.getFullName());
//    dto.setPhoneNumber(profile.getPhoneNumber());
//    dto.setAvatarUrl(profile.getAvatarUrl());
//    dto.setBio(profile.getBio());
//    dto.setAccountType(profile.getAccountType());
//    dto.setLocation(profile.getLocation());
//    dto.setRatingAvg(profile.getRatingAvg());
//    dto.setRatingCount(profile.getRatingCount());
//    dto.setSellerSince(profile.getSellerSince());
//    dto.setPreferences(profile.getPreferences());
//    dto.setCreatedAt(profile.getCreatedAt());
//    dto.setUpdatedAt(profile.getUpdatedAt());
//    return dto;
//}
}
}
