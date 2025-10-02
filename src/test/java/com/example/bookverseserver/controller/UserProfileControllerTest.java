package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.UserProfileService;
import com.example.bookverseserver.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private AuthenticationService authenticationService;

    private final Long TEST_USER_ID = 1L;

    @Test
    @WithMockUser
    void createProfile_whenAuthenticated_createsAndReturnsProfile() throws Exception {
        // Arrange
        when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);

        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setFullName("New User");
        profileResponse.setLocation("New Location");

        when(userProfileService.createProfileForUser(eq(TEST_USER_ID), any())).thenReturn(profileResponse);

        String requestBody = "{\"fullName\":\"New User\", \"location\":\"New Location\"}";

        // Act & Assert
        mockMvc.perform(post("/api/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullName", is("New User")))
                .andExpect(jsonPath("$.result.location", is("New Location")));
    }

    @Test
    @WithMockUser
    void getMyProfile_whenAuthenticated_returnsProfile() throws Exception {
        // Arrange
        when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);

        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setFullName("Test User");
        profileResponse.setLocation("Test Location");

        when(userProfileService.getProfileForUser(TEST_USER_ID)).thenReturn(profileResponse);

        // Act & Assert
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullName", is("Test User")))
                .andExpect(jsonPath("$.result.location", is("Test Location")));
    }

    @Test
    @WithMockUser
    void updateMyProfile_whenAuthenticated_updatesAndReturnsProfile() throws Exception {
        // Arrange
        when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);

        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setLocation("Updated Location");

        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setLocation("Updated Location");

        Authentication authentication = Mockito.mock(Authentication.class);
        when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);


        // Act & Assert
        mockMvc.perform(put("/api/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.location", is("Updated Location")));
    }
}