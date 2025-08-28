package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByEmailAndOtp(String email, String otp);

    Optional<EmailVerificationToken> findByEmail(String email);

    void deleteByEmail(String email);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
