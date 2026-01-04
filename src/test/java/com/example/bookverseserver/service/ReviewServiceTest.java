package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.Review.HelpfulVoteResponse;
import com.example.bookverseserver.dto.response.Review.ReviewResponse;
import com.example.bookverseserver.dto.response.Review.ReviewStatsResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Review;
import com.example.bookverseserver.entity.Product.ReviewHelpful;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.ReviewMapper;
import com.example.bookverseserver.repository.BookMetaRepository;
import com.example.bookverseserver.repository.ReviewHelpfulRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private ReviewHelpfulRepository reviewHelpfulRepository;
  @Mock
  private BookMetaRepository bookMetaRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReviewMapper reviewMapper;

  @InjectMocks
  private ReviewService reviewService;

  private User testUser;
  private BookMeta testBook;
  private Review testReview;
  private CreateReviewRequest createRequest;
  private UpdateReviewRequest updateRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    testBook = new BookMeta();
    testBook.setId(1L);
    testBook.setTitle("Test Book");

    testReview = Review.builder()
        .id(1L)
        .user(testUser)
        .bookMeta(testBook)
        .rating(5)
        .comment("Great book!")
        .isVisible(true)
        .isHidden(false)
        .helpfulCount(0)
        .verifiedPurchase(false)
        .build();

    createRequest = CreateReviewRequest.builder()
        .rating(5)
        .comment("Great book!")
        .build();

    updateRequest = UpdateReviewRequest.builder()
        .rating(4)
        .comment("Updated comment")
        .build();
  }

  @Test
  void createReview_Success() {
    // Given
    when(bookMetaRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reviewRepository.existsByUserIdAndBookMetaId(1L, 1L)).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(reviewMapper.toReview(createRequest)).thenReturn(testReview);
    when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

    ReviewResponse expectedResponse = ReviewResponse.builder()
        .id(1L)
        .bookId(1L)
        .userId(1L)
        .rating(5)
        .comment("Great book!")
        .build();
    when(reviewMapper.toReviewResponse(any(Review.class))).thenReturn(expectedResponse);
    when(reviewHelpfulRepository.existsByUserIdAndReviewId(anyLong(), anyLong())).thenReturn(false);

    // When
    ReviewResponse result = reviewService.createReview(1L, createRequest, 1L);

    // Then
    assertNotNull(result);
    assertEquals(5, result.getRating());
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  void createReview_DuplicateReview_ThrowsException() {
    // Given
    when(bookMetaRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reviewRepository.existsByUserIdAndBookMetaId(1L, 1L)).thenReturn(true);

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> reviewService.createReview(1L, createRequest, 1L));
    assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  void createReview_BookNotFound_ThrowsException() {
    // Given
    when(bookMetaRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> reviewService.createReview(1L, createRequest, 1L));
    assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void updateReview_Success() {
    // Given
    when(reviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testReview));
    when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

    ReviewResponse expectedResponse = ReviewResponse.builder()
        .id(1L)
        .rating(4)
        .comment("Updated comment")
        .build();
    when(reviewMapper.toReviewResponse(any(Review.class))).thenReturn(expectedResponse);
    when(reviewHelpfulRepository.existsByUserIdAndReviewId(anyLong(), anyLong())).thenReturn(false);

    // When
    ReviewResponse result = reviewService.updateReview(1L, updateRequest, 1L);

    // Then
    assertNotNull(result);
    verify(reviewMapper).updateReview(testReview, updateRequest);
    verify(reviewRepository).save(testReview);
  }

  @Test
  void updateReview_NotOwner_ThrowsException() {
    // Given
    when(reviewRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> reviewService.updateReview(1L, updateRequest, 2L));
    assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void deleteReview_Success() {
    // Given
    when(reviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testReview));

    // When
    reviewService.deleteReview(1L, 1L, false);

    // Then
    verify(reviewRepository).delete(testReview);
  }

  @Test
  void deleteReview_Admin_Success() {
    // Given
    when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

    // When
    reviewService.deleteReview(1L, 999L, true);

    // Then
    verify(reviewRepository).delete(testReview);
  }

  @Test
  void getBookRating_CalculatesCorrectly() {
    // Given
    when(bookMetaRepository.existsById(1L)).thenReturn(true);
    when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(4.5);
    when(reviewRepository.countByBookMetaIdAndIsVisibleTrueAndIsHiddenFalse(1L)).thenReturn(10L);

    List<Object[]> distribution = List.of(
        new Object[] { 5, 5L },
        new Object[] { 4, 3L },
        new Object[] { 3, 2L });
    when(reviewRepository.findRatingDistributionByBookId(1L)).thenReturn(distribution);

    // When
    ReviewStatsResponse result = reviewService.getBookRating(1L);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getBookId());
    assertEquals(4.5, result.getAverageRating());
    assertEquals(10, result.getTotalReviews());
    assertEquals(5, result.getRatingDistribution().get(5));
    assertEquals(3, result.getRatingDistribution().get(4));
    assertEquals(2, result.getRatingDistribution().get(3));
    assertEquals(0, result.getRatingDistribution().get(2));
    assertEquals(0, result.getRatingDistribution().get(1));
  }

  @Test
  void markHelpful_Success_AddVote() {
    // Given
    when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
    when(reviewHelpfulRepository.existsByUserIdAndReviewId(1L, 1L)).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(reviewHelpfulRepository.save(any(ReviewHelpful.class))).thenReturn(null);
    when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

    // When
    HelpfulVoteResponse result = reviewService.toggleHelpful(1L, 1L);

    // Then
    assertNotNull(result);
    assertTrue(result.getUserHasVoted());
    verify(reviewHelpfulRepository).save(any(ReviewHelpful.class));
  }

  @Test
  void markHelpful_Success_RemoveVote() {
    // Given
    testReview.setHelpfulCount(1);
    when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
    when(reviewHelpfulRepository.existsByUserIdAndReviewId(1L, 1L)).thenReturn(true);
    when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

    // When
    HelpfulVoteResponse result = reviewService.toggleHelpful(1L, 1L);

    // Then
    assertNotNull(result);
    assertFalse(result.getUserHasVoted());
    verify(reviewHelpfulRepository).deleteByUserIdAndReviewId(1L, 1L);
  }
}
