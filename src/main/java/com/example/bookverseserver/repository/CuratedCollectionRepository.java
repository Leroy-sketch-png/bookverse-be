package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.CuratedCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuratedCollectionRepository extends JpaRepository<CuratedCollection, Long> {
    Optional<CuratedCollection> findBySlug(String slug);

    List<CuratedCollection> findAllByOrderByCreatedAtDesc();

    boolean existsBySlug(String slug);

    @Query("SELECT cc FROM CuratedCollection cc LEFT JOIN FETCH cc.books WHERE cc.slug = :slug")
    Optional<CuratedCollection> findBySlugWithBooks(String slug);
}
