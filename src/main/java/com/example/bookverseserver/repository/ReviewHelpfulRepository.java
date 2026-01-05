package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.ReviewHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, Long> {

  // Check if user already voted helpful for a review
  boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

  // Find vote by user and review
  Optional<ReviewHelpful> findByUserIdAndReviewId(Long userId, Long reviewId);

  // Delete vote by user and review
  void deleteByUserIdAndReviewId(Long userId, Long reviewId);

  // Count helpful votes for a review
  long countByReviewId(Long reviewId);
}
