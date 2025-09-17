package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.mapper.UserProfileMapper;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService service;

    @Test
    void updateProfile_updatesAndReturnsResponse() {
        Long userId = 11L;
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setLocation("Hanoi");

        UserProfile existing = new UserProfile();
        existing.setId(1L);
        // set existing fields
        when(userProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(existing));

        // mapper updates in place: no return
        doAnswer(invocation -> {
            ProfileUpdateRequest dto = invocation.getArgument(0);
            UserProfile ent = invocation.getArgument(1);
            ent.setLocation(dto.getLocation());
            return null;
        }).when(userProfileMapper).updateFromDto(any(ProfileUpdateRequest.class), any(UserProfile.class));

        UserProfile saved = new UserProfile();
        saved.setId(1L);
        saved.setLocation("Hanoi");
        when(userProfileRepository.save(existing)).thenReturn(saved);

        ProfileResponse resp = new ProfileResponse();
        resp.setId(1L);
        resp.setUserId(userId);
        resp.setLocation("Hanoi");
        when(userProfileMapper.toResponse(saved)).thenReturn(resp);

        ProfileResponse out = service.updateProfileForUser(userId, req);

        assertNotNull(out);
        assertEquals("Hanoi", out.getLocation());

        verify(userProfileRepository).findByUser_Id(userId);
        verify(userProfileMapper).updateFromDto(req, existing);
        verify(userProfileRepository).save(existing);
    }
}
