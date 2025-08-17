package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken,Long> {
    boolean existsById(String id);
}