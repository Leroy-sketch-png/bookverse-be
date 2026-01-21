package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    Optional<CartItem> findByCartIdAndListingId(Long cartId, Long listingId);

    Optional<CartItem> findByCartUserIdAndListingId(Long userId, Long listingId);

    void deleteAllByCartId(Long id);
    
    /**
     * Clear cart items using direct SQL to avoid optimistic locking issues.
     * Use this after order creation when we don't need the entities anymore.
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    int deleteAllByCartIdDirect(@Param("cartId") Long cartId);
    
    // For seller analytics: count cart items for seller's listings
    long countByListingIdIn(List<Long> listingIds);
}
