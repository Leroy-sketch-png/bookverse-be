package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toEntity(ProfileCreationRequest request);

//    @Mapping(source = "user.id", target = "userId")
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
    ProfileResponse toResponse(UserProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ProfileUpdateRequest dto, @MappingTarget UserProfile profile);
}
