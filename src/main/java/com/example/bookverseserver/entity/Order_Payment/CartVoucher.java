package com.example.bookverseserver.entity.Order_Payment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "cart_voucher", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "voucher_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    Cart cart;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    Voucher voucher;
}
