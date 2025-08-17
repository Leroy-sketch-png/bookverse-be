package com.example.bookverseserver.entity.User;

import com.example.bookverseserver.entity.Order_Payment.Voucher;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_voucher", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "voucher_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    Voucher voucher;

    Integer timesUsed;
}
