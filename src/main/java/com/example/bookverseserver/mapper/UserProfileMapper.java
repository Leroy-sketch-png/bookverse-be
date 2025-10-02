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
}
