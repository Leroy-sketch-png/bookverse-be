package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser_Id(Long userId);
    
    // Alias for cleaner service code
    default Optional<UserProfile> findByUserId(Long userId) {
        return findByUser_Id(userId);
    }
    
    // Stripe integration finders
    Optional<UserProfile> findByStripeCustomerId(String stripeCustomerId);
    
    Optional<UserProfile> findByStripeAccountId(String stripeAccountId);
    
    Optional<UserProfile> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    // Admin stats: count by account type
    long countByAccountType(AccountType accountType);
    
    // Count PRO sellers
    long countByIsProSellerTrue();
}
