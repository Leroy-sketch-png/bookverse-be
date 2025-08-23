package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
    UserProfile toProfile(ProfileCreationRequest request);

//    @Mapping(target = "id", ignore = true)
//    //@Mapping(target = "user", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
    ProfileResponse toprofileResponse(UserProfile profile);

    //@Mapping(target = "id", source = "user.id")
    void updateProfile(@MappingTarget UserProfile profile, ProfileCreationRequest updatedProfile);
}
