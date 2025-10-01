package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    default UserProfile toEntity(ProfileCreationRequest request) {
        if (request == null) return null;

        return UserProfile.builder()
                .displayName(request.getDisplayName())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .bio(request.getBio())
                .accountType(request.getAccountType())
                .location(request.getLocation())
                .preferences(request.getPreferences())
                .build();
    }

//    @Mapping(target = "userId", expression = "java(profile.getUser() != null ? profile.getUser().getId() : null)")
//    @Mapping(source = "displayName", target = "displayName")
//    @Mapping(source = "phoneNumber", target = "phoneNumber")
//    @Mapping(source = "avatarUrl", target = "avatarUrl", ignore = true)
//    @Mapping(source = "bio", target = "bio", ignore = true)
//    @Mapping(source = "accountType", target = "accountType", ignore = true)
//    @Mapping(source = "location", target = "location")
//    @Mapping(source = "ratingAvg", target = "ratingAvg")
//    @Mapping(source = "ratingCount", target = "ratingCount")
//    @Mapping(source = "sellerSince", target = "sellerSince", ignore = true)
//    @Mapping(source = "preferences", target = "preferences")
//    @Mapping(source = "createdAt", target = "createdAt")
//    @Mapping(source = "updatedAt", target = "updatedAt")
default ProfileResponse toResponse(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        ProfileResponse dto = new ProfileResponse();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser() != null ? profile.getUser().getId() : null);
        dto.setDisplayName(profile.getDisplayName());
        dto.setFullName(profile.getFullName());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setBio(profile.getBio());
        dto.setAccountType(profile.getAccountType());
        dto.setLocation(profile.getLocation());
        dto.setRatingAvg(profile.getRatingAvg());
        dto.setRatingCount(profile.getRatingCount());
        dto.setSellerSince(profile.getSellerSince());
        dto.setPreferences(profile.getPreferences());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ProfileUpdateRequest dto, @MappingTarget UserProfile profile);
}
