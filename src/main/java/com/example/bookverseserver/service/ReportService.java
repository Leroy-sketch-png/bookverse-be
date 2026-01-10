package com.example.bookverseserver.service;

import com.example.bookverseserver.controller.ReportController.ReportSubmittedResponse;
import com.example.bookverseserver.dto.request.Moderation.CreateReportRequest;
import com.example.bookverseserver.dto.response.Moderation.UserReportResponse;
import com.example.bookverseserver.entity.Moderation.UserReport;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ReportPriority;
import com.example.bookverseserver.enums.ReportStatus;
import com.example.bookverseserver.enums.ReportType;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.OrderRepository;
import com.example.bookverseserver.repository.UserReportRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * ReportService - Handles user-submitted reports for trust & safety.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReportService {
    
    UserReportRepository userReportRepository;
    UserRepository userRepository;
    ListingRepository listingRepository;
    OrderRepository orderRepository;
    
    private static final Set<String> VALID_ENTITY_TYPES = Set.of("listing", "seller", "review");
    
    @Transactional
    public ReportSubmittedResponse createReport(User reporter, CreateReportRequest request) {
        // Validate entity type
        String entityType = request.getEntityType().toLowerCase();
        if (!VALID_ENTITY_TYPES.contains(entityType)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Build the report
        UserReport.UserReportBuilder reportBuilder = UserReport.builder()
                .reporter(reporter)
                .reportedEntityType(entityType)
                .reportedEntityId(request.getEntityId())
                .reportType(request.getReportType())
                .description(request.getDescription())
                .status(ReportStatus.OPEN)
                .priority(determinePriority(request.getReportType()));
        
        // Add evidence URLs if provided
        if (request.getEvidenceUrls() != null && !request.getEvidenceUrls().isEmpty()) {
            reportBuilder.evidenceUrls(request.getEvidenceUrls());
        }
        
        // Handle entity-specific lookups
        switch (entityType) {
            case "listing" -> {
                Listing listing = listingRepository.findById(request.getEntityId())
                        .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
                reportBuilder.reportedListing(listing);
                reportBuilder.reportedUser(listing.getSeller());
            }
            case "seller" -> {
                User seller = userRepository.findById(request.getEntityId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                reportBuilder.reportedUser(seller);
            }
            case "review" -> {
                // Review reports - reportedEntityId is the review ID
                // We don't have a direct reference, but we log it
                log.info("Review report for review ID: {}", request.getEntityId());
            }
        }
        
        // Handle related order if provided
        if (request.getRelatedOrderId() != null) {
            Order order = orderRepository.findById(request.getRelatedOrderId())
                    .orElse(null);
            if (order != null) {
                reportBuilder.relatedOrder(order);
            }
        }
        
        UserReport report = reportBuilder.build();
        report = userReportRepository.save(report);
        
        log.info("Report {} created by user {} for {} {}", 
                report.getId(), reporter.getId(), entityType, request.getEntityId());
        
        return ReportSubmittedResponse.builder()
                .reportId(report.getId())
                .status("SUBMITTED")
                .message("Your report has been submitted and will be reviewed within 24-48 hours.")
                .build();
    }
    
    /**
     * Determine report priority based on report type.
     * Fraud and safety issues get higher priority.
     */
    private ReportPriority determinePriority(ReportType reportType) {
        return switch (reportType) {
            case FRAUD, COUNTERFEIT, SAFETY_CONCERN -> ReportPriority.HIGH;
            case POLICY_VIOLATION, INAPPROPRIATE_CONTENT -> ReportPriority.MEDIUM;
            case SPAM, OTHER -> ReportPriority.LOW;
            default -> ReportPriority.MEDIUM;
        };
    }
    
    /**
     * Get reports submitted by the current user.
     */
    @Transactional(readOnly = true)
    public Page<UserReportResponse> getMyReports(User reporter, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, page - 1), 
                Math.min(limit, 50), 
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<UserReport> reports = userReportRepository.findByReporterId(reporter.getId(), pageRequest);
        return reports.map(this::toUserReportResponse);
    }
    
    /**
     * Convert UserReport entity to UserReportResponse DTO.
     */
    private UserReportResponse toUserReportResponse(UserReport r) {
        UserReportResponse.ReportedEntityInfo entityInfo = UserReportResponse.ReportedEntityInfo.builder()
                .type(r.getReportedEntityType())
                .id(r.getReportedEntityId())
                .name(r.getReportedUser() != null ? r.getReportedUser().getUsername() : "Unknown")
                .build();
        
        UserReportResponse.RelatedOrderInfo orderInfo = null;
        if (r.getRelatedOrder() != null) {
            orderInfo = UserReportResponse.RelatedOrderInfo.builder()
                    .id(r.getRelatedOrder().getId())
                    .orderNumber(r.getRelatedOrder().getOrderNumber())
                    .date(r.getRelatedOrder().getCreatedAt())
                    .build();
        }
        
        return UserReportResponse.builder()
                .id(r.getId())
                .reportedEntity(entityInfo)
                .reportType(r.getReportType())
                .description(r.getDescription())
                .evidenceUrls(r.getEvidenceUrls())
                .priority(r.getPriority())
                .status(r.getStatus())
                .relatedOrder(orderInfo)
                .resolutionNote(r.getResolutionNote())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}
