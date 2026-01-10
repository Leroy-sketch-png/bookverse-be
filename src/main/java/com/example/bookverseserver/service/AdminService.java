package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Admin.PlatformStatsResponse;
import com.example.bookverseserver.dto.response.Admin.ProApplicationDetailResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.User.ProSellerApplicationResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.ProSellerApplication;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ApplicationStatus;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserMapper;
import com.example.bookverseserver.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin service for platform management.
 * Per Vision features/admin.md
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    ListingRepository listingRepository;
    OrderRepository orderRepository;
    ProSellerApplicationRepository proApplicationRepository;
    FlaggedListingRepository flaggedListingRepository;
    DisputeRepository disputeRepository;
    UserProfileRepository userProfileRepository;
    TransactionRepository transactionRepository;

    /**
     * Assign a role to a user.
     * Used by ADMIN to promote users to MODERATOR, ADMIN, etc.
     * 
     * @param userId The user to assign the role to
     * @param roleName The role to assign
     * @param adminId The admin performing the action (for audit)
     * @return Updated user response
     */
    @Transactional
    public UserResponse assignRole(Long userId, RoleName roleName, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        // Add role to user's roles (don't replace existing roles)
        Set<Role> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        
        userRepository.save(user);
        
        log.info("Admin {} assigned role {} to user {}", adminId, roleName, userId);
        
        return userMapper.toUserResponse(user);
    }
    
    /**
     * Remove a role from a user.
     * 
     * @param userId The user to remove the role from
     * @param roleName The role to remove
     * @param adminId The admin performing the action
     * @return Updated user response
     */
    @Transactional
    public UserResponse removeRole(Long userId, RoleName roleName, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        Set<Role> roles = user.getRoles();
        roles.remove(role);
        user.setRoles(roles);
        
        userRepository.save(user);
        
        log.info("Admin {} removed role {} from user {}", adminId, roleName, userId);
        
        return userMapper.toUserResponse(user);
    }

    /**
     * Get platform-wide statistics for admin dashboard.
     * Per Vision API_CONTRACTS.md - GET /admin/stats
     */
    public PlatformStatsResponse getPlatformStats(int periodDays) {
        // User stats - REAL counts
        long totalUsers = userRepository.count();
        // Count by account type from UserProfile
        long proSellers = userProfileRepository.countByIsProSellerTrue();
        long sellers = userProfileRepository.countByAccountType(
                com.example.bookverseserver.enums.AccountType.SELLER) + proSellers;
        long buyers = totalUsers - sellers;

        // Listing stats
        long activeListings = listingRepository.countByStatus(ListingStatus.ACTIVE);
        long soldOutListings = listingRepository.countByStatus(ListingStatus.SOLD_OUT);
        long draftListings = listingRepository.countByStatus(ListingStatus.DRAFT);

        // Order stats
        long totalOrders = orderRepository.count();
        
        // Revenue stats - REAL calculation from completed payments
        BigDecimal totalRevenue = transactionRepository.sumAmountByStatus(
                com.example.bookverseserver.enums.PaymentStatus.PAID);
        // Platform fee at 8% standard or 3% for PRO (using simplified 5% avg)
        BigDecimal platformFee = totalRevenue.multiply(BigDecimal.valueOf(0.05));

        // Issue stats
        long pendingModeration = flaggedListingRepository.countByStatus(
            com.example.bookverseserver.enums.FlagStatus.PENDING
        );
        long pendingDisputes = disputeRepository.countByStatus(
            com.example.bookverseserver.enums.DisputeStatus.OPEN
        );
        long pendingProApplications = proApplicationRepository.findByStatus(
            ApplicationStatus.PENDING, PageRequest.of(0, 1)
        ).getTotalElements();

        return PlatformStatsResponse.builder()
                .users(PlatformStatsResponse.UserStats.builder()
                        .total(totalUsers)
                        .trend(0.0) // Trend calculation requires historical data
                        .buyers(buyers)
                        .sellers(sellers)
                        .proSellers(proSellers)
                        .build())
                .revenue(PlatformStatsResponse.RevenueStats.builder()
                        .total(totalRevenue)
                        .trend(0.0) // Trend calculation requires historical data
                        .platformFee(platformFee)
                        .transactionCount(totalOrders)
                        .build())
                .listings(PlatformStatsResponse.ListingStats.builder()
                        .active(activeListings)
                        .trend(0.0) // Trend calculation requires historical data
                        .available(activeListings + draftListings)
                        .sold(soldOutListings)
                        .build())
                .issues(PlatformStatsResponse.IssueStats.builder()
                        .pending(pendingModeration + pendingDisputes + pendingProApplications)
                        .trend(0.0) // Trend calculation requires historical data
                        .moderation(pendingModeration)
                        .disputes(pendingDisputes)
                        .verifications(pendingProApplications)
                        .build())
                .build();
    }

    /**
     * Get PRO seller applications for admin review.
     */
    public PagedResponse<ProApplicationDetailResponse> getProApplications(
            ApplicationStatus status, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("submittedAt").descending());
        
        Page<ProSellerApplication> applicationPage;
        if (status != null) {
            applicationPage = proApplicationRepository.findByStatus(status, pageable);
        } else {
            applicationPage = proApplicationRepository.findAllByOrderBySubmittedAtDesc(pageable);
        }

        List<ProApplicationDetailResponse> responses = applicationPage.getContent().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return PagedResponse.ofOneIndexed(
                responses,
                page,
                limit,
                applicationPage.getTotalElements(),
                applicationPage.getTotalPages()
        );
    }

    private ProApplicationDetailResponse toDetailResponse(ProSellerApplication app) {
        return ProApplicationDetailResponse.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .username(app.getUser().getUsername())
                .email(app.getUser().getEmail())
                .businessName(app.getBusinessName())
                .businessAddress(app.getBusinessAddress())
                .businessPhone(app.getBusinessPhone())
                .taxId(app.getTaxId())
                .businessLicenseNumber(app.getBusinessLicenseNumber())
                .businessDescription(app.getBusinessDescription())
                .yearsInBusiness(app.getYearsInBusiness())
                .monthlyInventory(app.getMonthlyInventory())
                .documentUrls(app.getDocumentUrls())
                .status(app.getStatus())
                .reviewNotes(app.getReviewNotes())
                .submittedAt(app.getSubmittedAt())
                .reviewedAt(app.getReviewedAt())
                .reviewedBy(app.getReviewedBy())
                .build();
    }

    /**
     * Get all users for admin management.
     * Supports search and role filtering (can combine both).
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsers(String search, RoleName role, int page, int limit) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, page - 1), 
                Math.min(limit, 50), 
                Sort.by("id").descending()
        );

        Page<User> userPage;
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasRole = role != null;
        
        if (hasSearch && hasRole) {
            // Combined: search + role filter
            String searchLower = "%" + search.toLowerCase() + "%";
            userPage = userRepository.searchUsersByRole(searchLower, role, pageable);
        } else if (hasSearch) {
            // Search only
            String searchLower = "%" + search.toLowerCase() + "%";
            userPage = userRepository.searchUsers(searchLower, pageable);
        } else if (hasRole) {
            // Role filter only
            userPage = userRepository.findByRoleName(role, pageable);
        } else {
            // No filters
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> responses = userPage.getContent().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        return PagedResponse.ofOneIndexed(
                responses,
                page,
                limit,
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    /**
     * Suspend a user account.
     */
    @Transactional
    public UserResponse suspendUser(Long userId, String reason, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Prevent suspending self
        if (userId.equals(adminId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("Admin {} suspended user {} for reason: {}", adminId, userId, reason);
        
        return userMapper.toUserResponse(user);
    }

    /**
     * Reactivate a suspended user account.
     */
    @Transactional
    public UserResponse reactivateUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        user.setEnabled(true);
        userRepository.save(user);
        
        log.info("Admin {} reactivated user {}", adminId, userId);
        
        return userMapper.toUserResponse(user);
    }
}
