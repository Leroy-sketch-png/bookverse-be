package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.request.User.ProfileUpdateRequest;
import com.example.bookverseserver.dto.request.User.UpgradeToSellerRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserProfileMapper;
import com.example.bookverseserver.repository.RoleRepository;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {

    UserProfileRepository userProfileRepository;
    UserRepository userRepository;
    UserProfileMapper userProfileMapper;
    CloudStorageService cloudStorageService;
    RoleRepository roleRepository;
    SecurityUtils securityUtils;

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
    public ProfileResponse updateProfileForUser(Authentication authentication, ProfileUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId(authentication);
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

    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        String avatarUrl = cloudStorageService.uploadFile(file);
        profile.setAvatarUrl(avatarUrl);
        userProfileRepository.save(profile);
        return avatarUrl;
    }

    @Transactional
    public void upgradeToSeller(Long userId, UpgradeToSellerRequest request) {
        if (!request.isAcceptTerms()) {
            throw new AppException(ErrorCode.TERMS_NOT_ACCEPTED);
        }

        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        User user = profile.getUser();

        Role casualRole = roleRepository.findByName(RoleName.CASUAL)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.getRoles().add(casualRole);
        profile.setAccountType("CASUAL");

        userRepository.save(user);
        userProfileRepository.save(profile);
    }
}
