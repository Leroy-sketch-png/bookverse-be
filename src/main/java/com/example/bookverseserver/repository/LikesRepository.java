package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikesRepository extends JpaRepository<Likes,Long> {

    boolean existsByUserIdAndListingId(Long currentUserId, Long listingId);

    void deleteByUserIdAndListingId(Long currentUserId, Long listingId);
}
