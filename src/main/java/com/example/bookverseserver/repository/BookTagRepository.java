package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookTagRepository extends JpaRepository<BookTag, Long> {
    
    Optional<BookTag> findBySlug(String slug);
    
    Optional<BookTag> findByNameIgnoreCase(String name);
    
    List<BookTag> findBySlugIn(List<String> slugs);
    
    /**
     * Find tags ordered by usage count (most popular first)
     */
    List<BookTag> findTop20ByOrderByUsageCountDesc();
    
    /**
     * Increment usage count when a book is tagged
     */
    @Modifying
    @Query("UPDATE BookTag t SET t.usageCount = t.usageCount + 1 WHERE t.id = :tagId")
    void incrementUsageCount(@Param("tagId") Long tagId);
}
