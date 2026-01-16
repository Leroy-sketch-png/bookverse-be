package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.User.ProSellerApplicationRequest;
import com.example.bookverseserver.dto.response.User.ProSellerApplicationResponse;
import com.example.bookverseserver.entity.User.ProSellerApplication;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.AccountType;
import com.example.bookverseserver.enums.ApplicationStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.ProSellerApplicationRepository;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * PRO Seller Application Service.
 * Per Vision features/pro-seller.md — Handles application submission, status checks, and admin review.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class ProSellerService {

    ProSellerApplicationRepository applicationRepository;
    UserRepository userRepository;
    UserProfileRepository userProfileRepository;

    /**
     * Submit PRO seller application.
     * Per Vision: POST /api/v1/sellers/me/pro-application
     */
    @Transactional
    public ProSellerApplicationResponse submitApplication(Long userId, ProSellerApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Check if user already has a pending application
        if (applicationRepository.existsByUserAndStatus(user, ApplicationStatus.PENDING)) {
            throw new AppException(ErrorCode.PRO_APPLICATION_ALREADY_PENDING);
        }
        
        // Check if user is already a PRO seller
        if (user.getUserProfile() != null && "PRO_SELLER".equals(user.getUserProfile().getAccountType())) {
            throw new AppException(ErrorCode.ALREADY_PRO_SELLER);
        }
        
        ProSellerApplication application = ProSellerApplication.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessAddress(request.getBusinessAddress())
                .businessPhone(request.getBusinessPhone())
                .taxId(request.getTaxId())
                .businessLicenseNumber(request.getBusinessLicenseNumber())
                .businessDescription(request.getBusinessDescription())
                .yearsInBusiness(request.getYearsInBusiness())
                .monthlyInventory(request.getMonthlyInventory())
                .documentUrls(request.getDocumentUrls() != null ? request.getDocumentUrls() : java.util.Collections.emptyList())
                .status(ApplicationStatus.PENDING)
                .build();
        
        application = applicationRepository.save(application);
        
        log.info("PRO seller application submitted by user {}: applicationId={}", userId, application.getId());
        
        return toResponse(application);
    }

    /**
     * Get current application status.
     * Per Vision: GET /api/v1/sellers/me/pro-application
     */
    public ProSellerApplicationResponse getApplicationStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        ProSellerApplication application = applicationRepository.findFirstByUserOrderBySubmittedAtDesc(user)
                .orElseThrow(() -> new AppException(ErrorCode.PRO_APPLICATION_NOT_FOUND));
        
        return toResponse(application);
    }

    /**
     * Admin: Approve PRO seller application.
     */
    @Transactional
    public ProSellerApplicationResponse approveApplication(Long applicationId, Long reviewerId, String notes) {
        ProSellerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.PRO_APPLICATION_NOT_FOUND));
        
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.PRO_APPLICATION_ALREADY_REVIEWED);
        }
        
        // Update application
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(reviewerId);
        application.setReviewNotes(notes);
        applicationRepository.save(application);
        
        // Upgrade user to PRO_SELLER
        User user = application.getUser();
        if (user.getUserProfile() != null) {
            user.getUserProfile().setAccountType("PRO_SELLER");
            userProfileRepository.save(user.getUserProfile());
        }
        
        log.info("PRO application {} approved by admin {}", applicationId, reviewerId);
        
        return toResponse(application);
    }

    /**
     * Admin: Reject PRO seller application.
     */
    @Transactional
    public ProSellerApplicationResponse rejectApplication(Long applicationId, Long reviewerId, String reason) {
        ProSellerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.PRO_APPLICATION_NOT_FOUND));
        
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.PRO_APPLICATION_ALREADY_REVIEWED);
        }
        
        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(reviewerId);
        application.setReviewNotes(reason);
        applicationRepository.save(application);
        
        log.info("PRO application {} rejected by admin {}: {}", applicationId, reviewerId, reason);
        
        return toResponse(application);
    }

    private ProSellerApplicationResponse toResponse(ProSellerApplication application) {
        return ProSellerApplicationResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .reviewNotes(application.getReviewNotes())
                .build();
    }
}
