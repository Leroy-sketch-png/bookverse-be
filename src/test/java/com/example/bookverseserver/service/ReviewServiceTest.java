package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.Review.HelpfulVoteResponse;
import com.example.bookverseserver.dto.response.Review.ReviewResponse;
import com.example.bookverseserver.dto.response.Review.ReviewStatsResponse;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Review;
import com.example.bookverseserver.entity.Product.ReviewHelpful;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.ReviewHelpfulRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.util.HtmlSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests for ReviewService (transaction-based marketplace model).
 * 
 * Reviews are on ORDER ITEMS (verified purchases) to build SELLER reputation.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewHelpfulRepository reviewHelpfulRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HtmlSanitizer htmlSanitizer;

    @InjectMocks
    private ReviewService reviewService;

    private User testBuyer;
    private User testSeller;
    private Listing testListing;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Review testReview;
    private CreateReviewRequest createRequest;
    private UpdateReviewRequest updateRequest;

    @BeforeEach
    void setUp() {
        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setUsername("buyer");

        testSeller = new User();
        testSeller.setId(2L);
        testSeller.setUsername("seller");

        testListing = Listing.builder()
                .id(100L)
                .seller(testSeller)
                .build();

        testOrder = Order.builder()
                .id(200L)
                .user(testBuyer)
                .status(OrderStatus.DELIVERED)
                .build();

        testOrderItem = OrderItem.builder()
                .id(300L)
                .order(testOrder)
                .listing(testListing)
                .seller(testSeller)
                .build();

        testReview = Review.builder()
                .id(1L)
                .user(testBuyer)
                .orderItem(testOrderItem)
                .listing(testListing)
                .seller(testSeller)
                .rating(5)
                .comment("Great seller!")
                .isVisible(true)
                .isHidden(false)
                .helpfulCount(0)
                .verifiedPurchase(true)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        // Mock HtmlSanitizer to return the same input (lenient to allow unused in some tests)
        lenient().when(htmlSanitizer.sanitizeBasic(any())).thenAnswer(i -> i.getArgument(0));

        createRequest = CreateReviewRequest.builder()
                .rating(5)
                .comment("Great seller!")
                .build();

        updateRequest = UpdateReviewRequest.builder()
                .rating(4)
                .comment("Updated comment")
                .build();
    }

    // =========================================================================
    // Create Review Tests (transaction-based)
    // =========================================================================

    @Nested
    @DisplayName("createReview tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review for delivered order item")
        void createReview_Success() {
            // Given
            when(orderItemRepository.findById(300L)).thenReturn(Optional.of(testOrderItem));
            when(reviewRepository.existsByOrderItemId(300L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testBuyer));
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

            // When
            ReviewResponse result = reviewService.createReview(200L, 300L, createRequest, 1L);

            // Then
            assertNotNull(result);
            assertEquals(5, result.getRating());
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should throw exception when order item not found")
        void createReview_OrderItemNotFound_ThrowsException() {
            // Given
            when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> reviewService.createReview(200L, 999L, createRequest, 1L));
            assertEquals(ErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception when order not delivered")
        void createReview_OrderNotDelivered_ThrowsException() {
            // Given
            testOrder.setStatus(OrderStatus.PROCESSING);
            when(orderItemRepository.findById(300L)).thenReturn(Optional.of(testOrderItem));

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> reviewService.createReview(200L, 300L, createRequest, 1L));
            assertEquals(ErrorCode.ORDER_NOT_DELIVERED, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception when review already exists")
        void createReview_DuplicateReview_ThrowsException() {
            // Given
            when(orderItemRepository.findById(300L)).thenReturn(Optional.of(testOrderItem));
            when(reviewRepository.existsByOrderItemId(300L)).thenReturn(true);

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> reviewService.createReview(200L, 300L, createRequest, 1L));
            assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception when user is not order owner")
        void createReview_NotOrderOwner_ThrowsException() {
            // Given
            when(orderItemRepository.findById(300L)).thenReturn(Optional.of(testOrderItem));

            // When & Then (user 999 is not the order owner)
            AppException exception = assertThrows(AppException.class,
                    () -> reviewService.createReview(200L, 300L, createRequest, 999L));
            assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        }
    }

    // =========================================================================
    // Update Review Tests
    // =========================================================================

    @Nested
    @DisplayName("updateReview tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review successfully")
        void updateReview_Success() {
            // Given
            when(reviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testReview));
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

            // When
            ReviewResponse result = reviewService.updateReview(1L, updateRequest, 1L);

            // Then
            assertNotNull(result);
            verify(reviewRepository).save(testReview);
        }

        @Test
        @DisplayName("Should throw exception when review not found or not owner")
        void updateReview_NotOwner_ThrowsException() {
            // Given
            when(reviewRepository.findByIdAndUserId(1L, 999L)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> reviewService.updateReview(1L, updateRequest, 999L));
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
        }
    }

    // =========================================================================
    // Delete Review Tests
    // =========================================================================

    @Nested
    @DisplayName("deleteReview tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete own review successfully")
        void deleteReview_Success() {
            // Given
            when(reviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testReview));

            // When
            reviewService.deleteReview(1L, 1L, false);

            // Then
            verify(reviewHelpfulRepository).deleteByReviewId(1L);
            verify(reviewRepository).delete(testReview);
        }

        @Test
        @DisplayName("Should allow admin to delete any review")
        void deleteReview_Admin_Success() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

            // When
            reviewService.deleteReview(1L, 999L, true);

            // Then
            verify(reviewHelpfulRepository).deleteByReviewId(1L);
            verify(reviewRepository).delete(testReview);
        }
    }

    // =========================================================================
    // Listing Stats Tests
    // =========================================================================

    @Nested
    @DisplayName("getListingReviewStats tests")
    class ListingStatsTests {

        @Test
        @DisplayName("Should calculate listing stats correctly")
        void getListingReviewStats_CalculatesCorrectly() {
            // Given
            when(reviewRepository.findAverageRatingByListingId(100L)).thenReturn(4.5);
            when(reviewRepository.countByListingIdAndIsVisibleTrueAndIsHiddenFalse(100L)).thenReturn(10L);

            List<Object[]> distribution = List.of(
                    new Object[]{5, 5L},
                    new Object[]{4, 3L},
                    new Object[]{3, 2L});
            when(reviewRepository.findRatingDistributionByListingId(100L)).thenReturn(distribution);

            // When
            ReviewStatsResponse result = reviewService.getListingReviewStats(100L);

            // Then
            assertNotNull(result);
            assertEquals(4.5, result.getAverageRating());
            assertEquals(10, result.getTotalReviews());
            assertEquals(5, result.getRatingDistribution().get(5));
            assertEquals(3, result.getRatingDistribution().get(4));
            assertEquals(2, result.getRatingDistribution().get(3));
        }
    }

    // =========================================================================
    // Helpful Vote Tests
    // =========================================================================

    @Nested
    @DisplayName("toggleHelpful tests")
    class HelpfulVoteTests {

        private User testVoter;

        @BeforeEach
        void setUpVoter() {
            // Voter must be different from review author (testBuyer id=1L)
            testVoter = new User();
            testVoter.setId(3L);
            testVoter.setUsername("voter");
        }

        @Test
        @DisplayName("Should add helpful vote when not voted")
        void toggleHelpful_AddVote() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(reviewHelpfulRepository.existsByUserIdAndReviewId(3L, 1L)).thenReturn(false);
            when(userRepository.findById(3L)).thenReturn(Optional.of(testVoter));
            when(reviewHelpfulRepository.save(any(ReviewHelpful.class))).thenReturn(null);
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

            // When
            HelpfulVoteResponse result = reviewService.toggleHelpful(1L, 3L);

            // Then
            assertNotNull(result);
            assertTrue(result.getUserHasVoted());
            verify(reviewHelpfulRepository).save(any(ReviewHelpful.class));
        }

        @Test
        @DisplayName("Should remove helpful vote when already voted")
        void toggleHelpful_RemoveVote() {
            // Given
            testReview.setHelpfulCount(1);
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(reviewHelpfulRepository.existsByUserIdAndReviewId(3L, 1L)).thenReturn(true);
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

            // When
            HelpfulVoteResponse result = reviewService.toggleHelpful(1L, 3L);

            // Then
            assertNotNull(result);
            assertFalse(result.getUserHasVoted());
            verify(reviewHelpfulRepository).deleteByUserIdAndReviewId(3L, 1L);
        }
    }
}
