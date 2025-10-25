package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    boolean existsByCode(String code);

    Optional<Voucher> findByCode(String code);
}
