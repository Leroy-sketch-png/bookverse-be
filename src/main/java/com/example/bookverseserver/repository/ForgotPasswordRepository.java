package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.ForgotPasswordOtpStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPasswordOtpStorage, Long> {
    Optional<ForgotPasswordOtpStorage> findByEmail(String email);

}
