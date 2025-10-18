package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.enums.ListingStatus;
import com.google.firebase.database.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByBookMetaAndStatusAndVisibility(com.example.bookverseserver.entity.Product.BookMeta bookMeta, ListingStatus status, boolean visibility);
    @NotNull
    @Query("""
    SELECT l 
    FROM Listing l
    LEFT JOIN FETCH l.bookMeta bm
    LEFT JOIN FETCH l.seller s
    LEFT JOIN FETCH l.photos p
    WHERE l.id = :id
""")
    Optional<Listing> findById(@NotNull @Param("id") Long id);
}
