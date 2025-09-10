package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserProfileMapper;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {

    UserProfileRepository userProfileRepository;
    UserRepository userRepository;
    UserProfileMapper userProfileMapper;

    @Transactional
    public ProfileResponse createProfileForUser(Long userId, ProfileCreationRequest request) {
        // ensure user exists
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // prevent duplicate profile
        Optional<UserProfile> existing = userProfileRepository.findByUser_Id(userId);
        if (existing.isPresent()) {
            throw new AppException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }
        UserProfile profile = userProfileMapper.toEntity(request);
        profile.setUser(user);
        UserProfile saved = userProfileRepository.save(profile);
        return userProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileForUser(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return userProfileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        return getProfileForUser(userId);
    }

    @Transactional
    public ProfileResponse updateProfileForUser(Long userId, ProfileUpdateRequest request) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        // MapStruct will ignore nulls so only provided fields are updated
        userProfileMapper.updateFromDto(request, profile);
        UserProfile saved = userProfileRepository.save(profile);
        return userProfileMapper.toResponse(saved);
    }

    @Transactional
    public void deleteProfileForUser(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        userProfileRepository.delete(profile);
    }
}
