package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.PayoutRequest;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.PayoutResponse;
import com.example.bookverseserver.dto.response.SellerBalanceResponse;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.Order_Payment.SellerPayout;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.SellerPayoutStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.PayoutMapper;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.SellerPayoutRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PayoutService {

    SellerPayoutRepository payoutRepository;
    OrderItemRepository orderItemRepository;
    UserRepository userRepository;
    PayoutMapper payoutMapper;
    SmsService smsService;
    StripeConnectService stripeConnectService;
    SecurityUtils securityUtils;

    private static final BigDecimal PRO_COMMISSION_RATE = new BigDecimal("0.03");  // 3%
    private static final BigDecimal CASUAL_COMMISSION_RATE = new BigDecimal("0.08"); // 8%
    private static final BigDecimal MINIMUM_PAYOUT = new BigDecimal("100000");

    /**
     * Get seller's balance information
     */
    @Transactional(readOnly = true)
    public SellerBalanceResponse getBalance(Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        
        // Get commission rate based on account type
        BigDecimal commissionRate = getCommissionRate(seller);
        
        // Calculate total earnings from delivered orders (after commission)
        BigDecimal grossEarnings = calculateGrossEarnings(seller);
        BigDecimal totalEarnings = grossEarnings.multiply(BigDecimal.ONE.subtract(commissionRate))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Get payout totals by status
        BigDecimal processingPayouts = payoutRepository.sumAmountBySellerAndStatusIn(
                seller, List.of(SellerPayoutStatus.PENDING, SellerPayoutStatus.PROCESSING));
        BigDecimal completedPayouts = payoutRepository.sumAmountBySellerAndStatus(
                seller, SellerPayoutStatus.COMPLETED);
        
        // Available = total earnings - processing - completed
        BigDecimal pendingBalance = totalEarnings.subtract(processingPayouts).subtract(completedPayouts);
        BigDecimal availableForPayout = pendingBalance.max(BigDecimal.ZERO);
        
        return SellerBalanceResponse.builder()
                .totalEarnings(totalEarnings)
                .pendingBalance(pendingBalance)
                .processingPayouts(processingPayouts)
                .completedPayouts(completedPayouts)
                .availableForPayout(availableForPayout)
                .commissionRate(commissionRate.multiply(new BigDecimal("100"))) // Display as percentage
                .minPayoutAmount(MINIMUM_PAYOUT) // Expose constant to frontend
                .build();
    }

    /**
     * Request a payout
     */
    @Transactional
    public PayoutResponse requestPayout(PayoutRequest request, Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        
        // Get available balance
        SellerBalanceResponse balance = getBalance(authentication);
        
        // Validate amount
        if (request.getAmount().compareTo(MINIMUM_PAYOUT) < 0) {
            throw new AppException(ErrorCode.MINIMUM_PAYOUT_AMOUNT);
        }
        
        if (request.getAmount().compareTo(balance.getAvailableForPayout()) > 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        // Create payout request
        SellerPayout payout = SellerPayout.builder()
                .seller(seller)
                .amount(request.getAmount())
                .status(SellerPayoutStatus.PENDING)
                .method(request.getMethod())
                .build();
        
        payout = payoutRepository.save(payout);
        log.info("Payout request created: {} for seller {} amount {}", 
                payout.getId(), seller.getId(), request.getAmount());
        
        return payoutMapper.toResponse(payout);
    }

    /**
     * Get seller's payout history
     */
    @Transactional(readOnly = true)
    public PagedResponse<PayoutResponse> getPayoutHistory(
            int page, int limit, Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<SellerPayout> payouts = payoutRepository.findBySellerOrderByCreatedAtDesc(seller, pageable);
        
        List<PayoutResponse> data = payoutMapper.toResponseList(payouts.getContent());
        
        return PagedResponse.ofOneIndexed(
                data,
                page,
                limit,
                payouts.getTotalElements(),
                payouts.getTotalPages()
        );
    }

    // ========== Admin methods ==========

    /**
     * Get pending payout requests for admin approval
     */
    @Transactional(readOnly = true)
    public PagedResponse<PayoutResponse> getPendingPayouts(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<SellerPayout> payouts = payoutRepository.findByStatusOrderByCreatedAtAsc(
                SellerPayoutStatus.PENDING, pageable);
        
        List<PayoutResponse> data = payoutMapper.toResponseList(payouts.getContent());
        
        return PagedResponse.ofOneIndexed(
                data,
                page,
                limit,
                payouts.getTotalElements(),
                payouts.getTotalPages()
        );
    }

    /**
     * Get payout history for a specific seller (admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<PayoutResponse> getPayoutHistoryBySellerId(Long sellerId, int page, int limit) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<SellerPayout> payouts = payoutRepository.findBySellerOrderByCreatedAtDesc(seller, pageable);
        
        List<PayoutResponse> data = payoutMapper.toResponseList(payouts.getContent());
        
        return PagedResponse.ofOneIndexed(
                data,
                page,
                limit,
                payouts.getTotalElements(),
                payouts.getTotalPages()
        );
    }

    /**
     * Approve a payout (admin only)
     */
    @Transactional
    public PayoutResponse approvePayout(Long payoutId) {
        SellerPayout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));
        
        if (payout.getStatus() != SellerPayoutStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_STATUS);
        }
        
        User seller = payout.getSeller();
        UserProfile profile = seller.getUserProfile();
        
        // Attempt real Stripe Connect transfer if seller has connected account
        String transferId = null;
        if (profile != null && profile.getStripeAccountId() != null) {
            // Convert to VND cents (smallest unit)
            long amountInCents = payout.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            transferId = stripeConnectService.transferToSeller(
                    profile.getStripeAccountId(),
                    amountInCents,
                    "PAYOUT-" + payoutId
            );
            log.info("Stripe transfer {} created for payout {}", transferId, payoutId);
        } else {
            log.info("Seller {} has no Connect account, simulating payout", seller.getId());
            transferId = "SIM-" + System.currentTimeMillis();
        }
        
        payout.setStatus(SellerPayoutStatus.COMPLETED);
        payout.setPaidAt(LocalDateTime.now());
        payout.setExternalReference(transferId);
        
        payout = payoutRepository.save(payout);
        log.info("Payout {} approved for seller {}", payoutId, payout.getSeller().getId());
        
        // Send SMS notification to seller
        sendPayoutStatusSms(payout, true);
        
        return payoutMapper.toResponse(payout);
    }

    /**
     * Reject a payout (admin only) with reason for seller transparency
     */
    @Transactional
    public PayoutResponse rejectPayout(Long payoutId, String reason) {
        SellerPayout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));
        
        if (payout.getStatus() != SellerPayoutStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_STATUS);
        }
        
        payout.setStatus(SellerPayoutStatus.FAILED);
        payout.setRejectionReason(reason);
        payout = payoutRepository.save(payout);
        log.info("Payout {} rejected for seller {} with reason: {}", payoutId, payout.getSeller().getId(), reason);
        
        // Send SMS notification to seller with reason
        sendPayoutStatusSms(payout, false);
        
        return payoutMapper.toResponse(payout);
    }

    /**
     * Get count of pending payouts (for admin dashboard)
     */
    @Transactional(readOnly = true)
    public long getPendingPayoutCount() {
        return payoutRepository.countByStatus(SellerPayoutStatus.PENDING);
    }

    // ========== Helper methods ==========

    private User getCurrentSeller(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Verify user is a seller
        UserProfile profile = user.getUserProfile();
        if (profile == null || "BUYER".equals(profile.getAccountType())) {
            throw new AppException(ErrorCode.NOT_A_SELLER);
        }
        
        return user;
    }

    private BigDecimal getCommissionRate(User seller) {
        UserProfile profile = seller.getUserProfile();
        if (profile != null && "PRO_SELLER".equals(profile.getAccountType())) {
            return PRO_COMMISSION_RATE;
        }
        return CASUAL_COMMISSION_RATE;
    }

    private BigDecimal calculateGrossEarnings(User seller) {
        List<OrderItem> orderItems = orderItemRepository.findBySeller(seller);
        
        return orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Send SMS notification about payout status
     */
    private void sendPayoutStatusSms(SellerPayout payout, boolean approved) {
        try {
            User seller = payout.getSeller();
            UserProfile profile = seller.getUserProfile();
            String phoneNumber = null;
            
            // Try to get phone from profile
            if (profile != null && profile.getPhoneNumber() != null 
                    && !profile.getPhoneNumber().isBlank()) {
                phoneNumber = profile.getPhoneNumber();
            }
            
            if (phoneNumber == null) {
                log.debug("No phone number available for seller {} - skipping SMS", seller.getId());
                return;
            }
            
            String sellerName = profile != null && profile.getDisplayName() != null 
                    ? profile.getDisplayName() 
                    : seller.getUsername();
            String formattedAmount = "$" + payout.getAmount().setScale(2).toPlainString();
            
            if (approved) {
                smsService.sendPayoutNotification(phoneNumber, formattedAmount, "COMPLETED");
                log.info("Sent payout success SMS to seller {}", seller.getId());
            } else {
                // Use a generic notification for rejected payouts
                String message = String.format(
                    "Hi %s, your payout request for %s was not approved. Please contact support for details.",
                    sellerName, formattedAmount
                );
                smsService.sendSms(phoneNumber, message);
                log.info("Sent payout rejection SMS to seller {}", seller.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to send payout SMS notification: {}", e.getMessage());
            // Don't fail the payout operation if SMS fails
        }
    }
}
