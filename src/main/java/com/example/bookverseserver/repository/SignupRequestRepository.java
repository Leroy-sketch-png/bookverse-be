package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.SignupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SignupRequestRepository extends JpaRepository<SignupRequest, Long> {
    Optional<SignupRequest> findByEmail(String email);

    void deleteByEmail(String email);

    @Modifying
    @Query("DELETE FROM SignupRequest r WHERE r.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);

}
