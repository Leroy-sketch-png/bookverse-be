package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileMapperTest {

    private UserProfileMapper userProfileMapper;

    @BeforeEach
    void setUp() {
        userProfileMapper = new UserProfileMapperImpl();
    }

    @Test
    void testToEntity() {
        ProfileCreationRequest request = new ProfileCreationRequest();
        request.setFullName("Test User");
        request.setLocation("Test Location");
        request.setBio("Test Bio");

        UserProfile entity = userProfileMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isEqualTo("Test User");
        assertThat(entity.getLocation()).isEqualTo("Test Location");
        assertThat(entity.getBio()).isEqualTo("Test Bio");
    }

    @Test
    void testToResponse() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFullName("Test User");
        profile.setLocation("Test Location");
        profile.setBio("Test Bio");

        User user = new User();
        user.setId(123L);
        profile.setUser(user);

        ProfileResponse response = userProfileMapper.toResponse(profile);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(123L);
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getLocation()).isEqualTo("Test Location");
        assertThat(response.getBio()).isEqualTo("Test Bio");
    }

    @Test
    void testUpdateFromDto() {
        ProfileUpdateRequest dto = new ProfileUpdateRequest();
        dto.setLocation("New Location");
        dto.setBio("New Bio");

        UserProfile profile = new UserProfile();
        profile.setFullName("Old Name");
        profile.setLocation("Old Location");
        profile.setBio("Old Bio");
        profile.setAvatarUrl("old_avatar.jpg");

        userProfileMapper.updateFromDto(dto, profile);

        assertThat(profile.getFullName()).isEqualTo("Old Name");
        assertThat(profile.getLocation()).isEqualTo("New Location");
        assertThat(profile.getBio()).isEqualTo("New Bio");
        assertThat(profile.getAvatarUrl()).isEqualTo("old_avatar.jpg");
    }
}
