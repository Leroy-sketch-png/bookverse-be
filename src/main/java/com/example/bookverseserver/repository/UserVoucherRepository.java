package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    
    /**
     * Find a user's voucher usage record.
     */
    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);
    
    /**
     * Count how many times a user has used a specific voucher.
     */
    @Query("SELECT COALESCE(uv.timesUsed, 0) FROM UserVoucher uv WHERE uv.user.id = :userId AND uv.voucher.id = :voucherId")
    Optional<Integer> countUsageByUserAndVoucher(@Param("userId") Long userId, @Param("voucherId") Long voucherId);
    
    /**
     * Atomically increment voucher usage count for a user.
     * Creates the record if it doesn't exist (handled in service).
     */
    @Modifying
    @Query("UPDATE UserVoucher uv SET uv.timesUsed = uv.timesUsed + 1 WHERE uv.user.id = :userId AND uv.voucher.id = :voucherId")
    int incrementUsage(@Param("userId") Long userId, @Param("voucherId") Long voucherId);
}
