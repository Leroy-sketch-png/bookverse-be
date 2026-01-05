package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  // Find reviews by book (paginated, visible only)
  Page<Review> findByBookMetaIdAndIsVisibleTrueAndIsHiddenFalse(Long bookId, Pageable pageable);

  // Find reviews by book filtered by rating
  Page<Review> findByBookMetaIdAndRatingAndIsVisibleTrueAndIsHiddenFalse(Long bookId, Integer rating,
      Pageable pageable);

  // Find reviews by user (paginated)
  Page<Review> findByUserId(Long userId, Pageable pageable);

  // Check if user already reviewed this book
  boolean existsByUserIdAndBookMetaId(Long userId, Long bookId);

  // Find review by ID and user (for owner check)
  Optional<Review> findByIdAndUserId(Long id, Long userId);

  // Find review by user and book
  Optional<Review> findByUserIdAndBookMetaId(Long userId, Long bookId);

  // Count reviews by book
  long countByBookMetaIdAndIsVisibleTrueAndIsHiddenFalse(Long bookId);

  // Get average rating for a book
  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookMeta.id = :bookId AND r.isVisible = true AND r.isHidden = false")
  Double findAverageRatingByBookId(@Param("bookId") Long bookId);

  // Get rating distribution for a book
  @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.bookMeta.id = :bookId AND r.isVisible = true AND r.isHidden = false GROUP BY r.rating")
  List<Object[]> findRatingDistributionByBookId(@Param("bookId") Long bookId);

  // Find all reviews by book (for admin)
  Page<Review> findByBookMetaId(Long bookId, Pageable pageable);
}
