package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Moderation.ModerationActionRequest;
import com.example.bookverseserver.dto.response.Moderation.*;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.entity.Moderation.*;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.*;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Moderation Service - per Vision features/moderation.md.
 * Handles flagged listings, user reports, disputes, and moderation actions.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ModerationService {

    FlaggedListingRepository flaggedListingRepository;
    UserReportRepository userReportRepository;
    DisputeRepository disputeRepository;
    ModerationActionRepository moderationActionRepository;
    WarningRepository warningRepository;
    SuspensionRepository suspensionRepository;
    ListingRepository listingRepository;
    UserRepository userRepository;

    // ============ Dashboard Summary ============

    @Transactional(readOnly = true)
    public ModerationSummary getSummary() {
        return ModerationSummary.builder()
                .flaggedListings(ModerationSummary.QueueStats.builder()
                        .pending(flaggedListingRepository.countByStatus(FlagStatus.PENDING))
                        .reviewing(flaggedListingRepository.countByStatus(FlagStatus.REVIEWING))
                        .critical(flaggedListingRepository.countBySeverity(FlagSeverity.CRITICAL))
                        .build())
                .reports(ModerationSummary.QueueStats.builder()
                        .pending(userReportRepository.countByStatus(ReportStatus.OPEN))
                        .reviewing(userReportRepository.countByStatus(ReportStatus.INVESTIGATING))
                        .critical(userReportRepository.countByPriority(ReportPriority.CRITICAL))
                        .build())
                .disputes(ModerationSummary.QueueStats.builder()
                        .pending(disputeRepository.countByStatus(DisputeStatus.OPEN))
                        .reviewing(disputeRepository.countByStatus(DisputeStatus.INVESTIGATING))
                        .critical(0L) // Disputes don't have severity
                        .build())
                .build();
    }

    // ============ Flagged Listings ============

    @Transactional(readOnly = true)
    public PagedResponse<FlaggedListingResponse> getFlaggedListings(
            FlagStatus status,
            FlagSeverity severity,
            FlagType flagType,
            int page,
            int limit) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("flaggedAt").descending());
        
        Page<FlaggedListing> flaggedPage;
        if (status != null) {
            flaggedPage = flaggedListingRepository.findByStatusOrderedByPriority(status, pageRequest);
        } else {
            flaggedPage = flaggedListingRepository.findAll(pageRequest);
        }
        
        List<FlaggedListingResponse> responses = flaggedPage.getContent().stream()
                .map(this::toFlaggedListingResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                responses,
                flaggedPage.getNumber(),
                flaggedPage.getSize(),
                flaggedPage.getTotalElements(),
                flaggedPage.getTotalPages()
        );
    }

    @Transactional
    public FlaggedListingResponse reviewFlaggedListing(
            Long moderatorId,
            Long flagId,
            ModerationActionRequest request) {
        
        FlaggedListing flagged = flaggedListingRepository.findById(flagId)
                .orElseThrow(() -> new AppException(ErrorCode.FLAGGED_LISTING_NOT_FOUND));
        
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Update flag status
        flagged.setStatus(FlagStatus.RESOLVED);
        flagged.setReviewedBy(moderator);
        flagged.setReviewedAt(LocalDateTime.now());
        flagged.setReviewNote(request.getNote());
        
        // Take action based on request
        handleFlaggedListingAction(moderator, flagged, request);
        
        FlaggedListing saved = flaggedListingRepository.save(flagged);
        
        // Record moderation action
        recordAction(moderator, request.getAction(), "flagged_listing", flagId, 
                flagged.getListing().getSeller(), request.getReason(), request.getNote());
        
        return toFlaggedListingResponse(saved);
    }

    // ============ User Reports ============

    @Transactional(readOnly = true)
    public PagedResponse<UserReportResponse> getReports(
            ReportStatus status,
            ReportType type,
            ReportPriority priority,
            int page,
            int limit) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        
        Page<UserReport> reportsPage;
        if (status != null) {
            reportsPage = userReportRepository.findByStatusOrderedByPriority(status, pageRequest);
        } else {
            reportsPage = userReportRepository.findAll(pageRequest);
        }
        
        List<UserReportResponse> responses = reportsPage.getContent().stream()
                .map(this::toUserReportResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                responses,
                reportsPage.getNumber(),
                reportsPage.getSize(),
                reportsPage.getTotalElements(),
                reportsPage.getTotalPages()
        );
    }

    @Transactional
    public UserReportResponse takeActionOnReport(
            Long moderatorId,
            Long reportId,
            ModerationActionRequest request) {
        
        UserReport report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_REPORT_NOT_FOUND));
        
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Update report status
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolutionNote(request.getNote());
        report.setResolvedAt(LocalDateTime.now());
        
        // Take action on reported entity
        handleReportAction(moderator, report, request);
        
        UserReport saved = userReportRepository.save(report);
        
        // Record moderation action
        recordAction(moderator, request.getAction(), "user_report", reportId,
                report.getReportedUser(), request.getReason(), request.getNote());
        
        return toUserReportResponse(saved);
    }

    // ============ Disputes ============

    @Transactional(readOnly = true)
    public PagedResponse<DisputeResponse> getDisputes(
            DisputeStatus status,
            int page,
            int limit) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        
        Page<Dispute> disputesPage;
        if (status != null) {
            disputesPage = disputeRepository.findByStatus(status, pageRequest);
        } else {
            disputesPage = disputeRepository.findAll(pageRequest);
        }
        
        List<DisputeResponse> responses = disputesPage.getContent().stream()
                .map(this::toDisputeResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                responses,
                disputesPage.getNumber(),
                disputesPage.getSize(),
                disputesPage.getTotalElements(),
                disputesPage.getTotalPages()
        );
    }

    @Transactional
    public DisputeResponse resolveDispute(
            Long moderatorId,
            Long disputeId,
            ModerationActionRequest request) {
        
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Update dispute
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolution(request.getNote());
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setAssignedTo(moderator);
        
        // Handle refund if requested
        if (Boolean.TRUE.equals(request.getRefundBuyer())) {
            dispute.setRefundAmount(dispute.getDisputedAmount());
            // TODO: Trigger actual refund via payment service
        }
        
        Dispute saved = disputeRepository.save(dispute);
        
        // Record moderation action
        recordAction(moderator, request.getAction(), "dispute", disputeId,
                dispute.getSeller(), request.getReason(), request.getNote());
        
        return toDisputeResponse(saved);
    }

    // ============ Private Helpers ============

    private void handleFlaggedListingAction(User moderator, FlaggedListing flagged, ModerationActionRequest request) {
        Listing listing = flagged.getListing();
        User seller = listing.getSeller();
        
        switch (request.getAction()) {
            case APPROVE -> {
                listing.setStatus(ListingStatus.ACTIVE);
                listingRepository.save(listing);
            }
            case REMOVE_LISTING, BAN_LISTING -> {
                listing.setStatus(ListingStatus.REMOVED);
                listing.setDeletedAt(LocalDateTime.now());
                listingRepository.save(listing);
                
                if (Boolean.TRUE.equals(request.getIssueWarning())) {
                    issueWarning(moderator, seller, request.getReason(), "Listing removed: " + listing.getId());
                }
            }
            case SUSPEND_USER -> {
                suspendUser(moderator, seller, request.getSuspensionDays(), request.getReason());
            }
            case BAN_USER -> {
                banUser(moderator, seller, request.getReason());
            }
            default -> {
                // DISMISS, REQUEST_CHANGES - just update status
            }
        }
    }

    private void handleReportAction(User moderator, UserReport report, ModerationActionRequest request) {
        User reportedUser = report.getReportedUser();
        
        if (reportedUser == null) return;
        
        switch (request.getAction()) {
            case WARN_USER -> {
                issueWarning(moderator, reportedUser, request.getReason(), report.getDescription());
            }
            case SUSPEND_USER -> {
                suspendUser(moderator, reportedUser, request.getSuspensionDays(), request.getReason());
            }
            case BAN_USER -> {
                banUser(moderator, reportedUser, request.getReason());
            }
            case REMOVE_LISTING -> {
                if (report.getReportedListing() != null) {
                    Listing listing = report.getReportedListing();
                    listing.setStatus(ListingStatus.REMOVED);
                    listing.setDeletedAt(LocalDateTime.now());
                    listingRepository.save(listing);
                }
            }
            default -> {
                // DISMISS, APPROVE, etc.
            }
        }
    }

    private void issueWarning(User moderator, User user, String reason, String description) {
        Warning warning = Warning.builder()
                .user(user)
                .issuedBy(moderator)
                .reason(reason)
                .description(description)
                .severity("MEDIUM")
                .expiresAt(LocalDateTime.now().plusMonths(6))
                .build();
        
        warningRepository.save(warning);
        log.info("Issued warning to user {} by moderator {}", user.getId(), moderator.getId());
    }

    private void suspendUser(User moderator, User user, Integer days, String reason) {
        // Deactivate any existing suspension
        suspensionRepository.findByUserIdAndIsActiveTrue(user.getId())
                .ifPresent(s -> {
                    s.setIsActive(false);
                    suspensionRepository.save(s);
                });
        
        Suspension suspension = Suspension.builder()
                .user(user)
                .suspendedBy(moderator)
                .reason(reason)
                .description("Suspended for policy violation")
                .isPermanent(false)
                .durationDays(days != null ? days : 7)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusDays(days != null ? days : 7))
                .isActive(true)
                .build();
        
        suspensionRepository.save(suspension);
        
        // Disable user account
        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("Suspended user {} for {} days by moderator {}", user.getId(), days, moderator.getId());
    }

    private void banUser(User moderator, User user, String reason) {
        Suspension suspension = Suspension.builder()
                .user(user)
                .suspendedBy(moderator)
                .reason(reason)
                .description("Permanent ban for severe policy violation")
                .isPermanent(true)
                .startsAt(LocalDateTime.now())
                .isActive(true)
                .build();
        
        suspensionRepository.save(suspension);
        
        // Disable user account
        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("Permanently banned user {} by moderator {}", user.getId(), moderator.getId());
    }

    private void recordAction(User moderator, ModerationActionType actionType, String targetType, 
                              Long targetId, User affectedUser, String reason, String note) {
        ModerationAction action = ModerationAction.builder()
                .moderator(moderator)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .affectedUser(affectedUser)
                .reason(reason != null ? reason : actionType.name())
                .note(note)
                .build();
        
        moderationActionRepository.save(action);
    }

    // ============ DTO Mappers ============

    private FlaggedListingResponse toFlaggedListingResponse(FlaggedListing f) {
        Listing listing = f.getListing();
        User seller = listing.getSeller();
        
        return FlaggedListingResponse.builder()
                .id(f.getId())
                .listing(FlaggedListingResponse.ListingSummary.builder()
                        .id(listing.getId())
                        .title(listing.getBookMeta() != null ? listing.getBookMeta().getTitle() : "Unknown")
                        .price(listing.getPrice())
                        .imageUrl(listing.getPhotos() != null && !listing.getPhotos().isEmpty() 
                                ? listing.getPhotos().iterator().next().getUrl() : null)
                        .seller(FlaggedListingResponse.SellerSummary.builder()
                                .id(seller.getId())
                                .name(seller.getUsername())
                                .rating(null) // TODO: Get from profile
                                .joinedAt(seller.getCreatedAt())
                                .listingCount(null) // TODO: Calculate
                                .build())
                        .build())
                .flagType(f.getFlagType())
                .flagReason(f.getFlagReason())
                .confidenceScore(f.getConfidenceScore())
                .severity(f.getSeverity())
                .status(f.getStatus())
                .reviewNote(f.getReviewNote())
                .flaggedAt(f.getFlaggedAt())
                .reviewedAt(f.getReviewedAt())
                .build();
    }

    private UserReportResponse toUserReportResponse(UserReport r) {
        return UserReportResponse.builder()
                .id(r.getId())
                .reporter(UserReportResponse.ReporterInfo.builder()
                        .id(r.getReporter().getId())
                        .name(r.getReporter().getUsername())
                        .reportHistory(UserReportResponse.ReportHistory.builder()
                                .submitted(0) // TODO: Calculate
                                .valid(0)
                                .build())
                        .build())
                .reportedEntity(UserReportResponse.ReportedEntityInfo.builder()
                        .type(r.getReportedEntityType())
                        .id(r.getReportedEntityId())
                        .name(r.getReportedUser() != null ? r.getReportedUser().getUsername() : null)
                        .build())
                .reportType(r.getReportType())
                .description(r.getDescription())
                .evidenceUrls(r.getEvidenceUrls())
                .priority(r.getPriority())
                .status(r.getStatus())
                .relatedOrder(r.getRelatedOrder() != null ? UserReportResponse.RelatedOrderInfo.builder()
                        .id(r.getRelatedOrder().getId())
                        .orderNumber(r.getRelatedOrder().getOrderNumber())
                        .date(r.getRelatedOrder().getCreatedAt())
                        .build() : null)
                .resolutionNote(r.getResolutionNote())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }

    private DisputeResponse toDisputeResponse(Dispute d) {
        return DisputeResponse.builder()
                .id(d.getId())
                .order(DisputeResponse.OrderInfo.builder()
                        .id(d.getOrder().getId())
                        .orderNumber(d.getOrder().getOrderNumber())
                        .total(d.getOrder().getTotalAmount())
                        .date(d.getOrder().getCreatedAt())
                        .build())
                .buyer(DisputeResponse.PartyInfo.builder()
                        .id(d.getBuyer().getId())
                        .name(d.getBuyer().getUsername())
                        .email(d.getBuyer().getEmail())
                        .build())
                .seller(DisputeResponse.PartyInfo.builder()
                        .id(d.getSeller().getId())
                        .name(d.getSeller().getUsername())
                        .email(d.getSeller().getEmail())
                        .build())
                .reason(d.getReason())
                .description(d.getDescription())
                .disputedAmount(d.getDisputedAmount())
                .evidenceUrls(d.getEvidenceUrls())
                .status(d.getStatus())
                .sellerResponse(d.getSellerResponse())
                .sellerRespondedAt(d.getSellerRespondedAt())
                .resolution(d.getResolution())
                .refundAmount(d.getRefundAmount())
                .createdAt(d.getCreatedAt())
                .resolvedAt(d.getResolvedAt())
                .build();
    }
}
