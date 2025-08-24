package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.entity.User.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByEmailAndOtp(String email, String otp);
}

