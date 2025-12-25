package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    Optional<CartItem> findByCartIdAndListingId(Long cartId, Long listingId);

    Optional<CartItem> findByCartUserIdAndListingId(Long userId, Long listingId);

    void deleteAllByCartId(Long id);
}
