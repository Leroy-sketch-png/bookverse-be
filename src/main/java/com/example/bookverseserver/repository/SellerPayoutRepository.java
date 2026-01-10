package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.SellerPayout;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.SellerPayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SellerPayoutRepository extends JpaRepository<SellerPayout, Long> {

    Page<SellerPayout> findBySellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    List<SellerPayout> findBySellerAndStatusIn(User seller, List<SellerPayoutStatus> statuses);

    @Query("SELECT COALESCE(SUM(sp.amount), 0) FROM SellerPayout sp WHERE sp.seller = :seller AND sp.status = :status")
    BigDecimal sumAmountBySellerAndStatus(@Param("seller") User seller, @Param("status") SellerPayoutStatus status);

    @Query("SELECT COALESCE(SUM(sp.amount), 0) FROM SellerPayout sp WHERE sp.seller = :seller AND sp.status IN :statuses")
    BigDecimal sumAmountBySellerAndStatusIn(@Param("seller") User seller, @Param("statuses") List<SellerPayoutStatus> statuses);

    // Admin queries
    Page<SellerPayout> findByStatusOrderByCreatedAtAsc(SellerPayoutStatus status, Pageable pageable);

    long countByStatus(SellerPayoutStatus status);
}
