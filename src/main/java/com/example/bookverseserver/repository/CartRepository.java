package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {
    /**
     * Fetch cart with all nested relations needed for CartItemMapper.
     * MUST include seller and userProfile to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.listing l " +
            "LEFT JOIN FETCH l.seller s " +
            "LEFT JOIN FETCH s.userProfile " +
            "LEFT JOIN FETCH l.photos " +
            "LEFT JOIN FETCH l.bookMeta bm " +
            "LEFT JOIN FETCH bm.authors " +
            "LEFT JOIN FETCH bm.images " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
}
