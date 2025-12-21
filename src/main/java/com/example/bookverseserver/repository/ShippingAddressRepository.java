package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByUserId(Long userId);

    Optional<ShippingAddress> findByIdAndUserId(Long id, Long userId);

    Optional<ShippingAddress> findByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("UPDATE ShippingAddress s SET s.isDefault = false WHERE s.user.id = :userId AND s.isDefault = true")
    void resetDefaultAddress(@Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}

