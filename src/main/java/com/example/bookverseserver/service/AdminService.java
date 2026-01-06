package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Admin.PlatformStatsResponse;
import com.example.bookverseserver.dto.response.Admin.ProApplicationDetailResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.User.ProSellerApplicationResponse;
import com.example.bookverseserver.entity.User.ProSellerApplication;
import com.example.bookverseserver.enums.ApplicationStatus;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
    ListingRepository listingRepository;
    OrderRepository orderRepository;
    ProSellerApplicationRepository proApplicationRepository;
    FlaggedListingRepository flaggedListingRepository;
    DisputeRepository disputeRepository;

    /**
     * Get platform-wide statistics for admin dashboard.
     * Per Vision API_CONTRACTS.md - GET /admin/stats
     */
    public PlatformStatsResponse getPlatformStats(int periodDays) {
        // User stats
        long totalUsers = userRepository.count();
        // Simplified: count by account type would require profile repository query
        // For now, using placeholder estimates
        long sellers = totalUsers / 3; // ~33% are sellers (estimate)
        long proSellers = sellers / 10; // ~10% of sellers are PRO (estimate)
        long buyers = totalUsers - sellers;

        // Listing stats
        long activeListings = listingRepository.countByStatus(ListingStatus.ACTIVE);
        long soldOutListings = listingRepository.countByStatus(ListingStatus.SOLD_OUT);
        long draftListings = listingRepository.countByStatus(ListingStatus.DRAFT);

        // Order stats (simplified)
        long totalOrders = orderRepository.count();

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
                        .trend(5.2) // Placeholder
                        .buyers(buyers)
                        .sellers(sellers)
                        .proSellers(proSellers)
                        .build())
                .revenue(PlatformStatsResponse.RevenueStats.builder()
                        .total(BigDecimal.valueOf(285000000)) // Placeholder
                        .trend(18.2) // Placeholder
                        .platformFee(BigDecimal.valueOf(12825000)) // Placeholder
                        .transactionCount(totalOrders)
                        .build())
                .listings(PlatformStatsResponse.ListingStats.builder()
                        .active(activeListings)
                        .trend(15.1) // Placeholder
                        .available(activeListings + draftListings)
                        .sold(soldOutListings)
                        .build())
                .issues(PlatformStatsResponse.IssueStats.builder()
                        .pending(pendingModeration + pendingDisputes + pendingProApplications)
                        .trend(-5.0) // Placeholder
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
}
