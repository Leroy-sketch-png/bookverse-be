package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.SellerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerSettingsRepository extends JpaRepository<SellerSettings, Long> {
    
    Optional<SellerSettings> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}
