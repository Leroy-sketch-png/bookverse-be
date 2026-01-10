package com.example.bookverseserver.entity.User;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "shipping_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(name = "phone_number", nullable = false, length = 20)
    String phoneNumber;

    @Column(name = "address_line1", nullable = false)
    String addressLine1;

    @Column(name = "address_line2")
    String addressLine2;

    @Column(nullable = false, length = 100)
    String city;
    
    @Column(name = "province_id")
    Integer provinceId;
    
    @Column(length = 100)
    String ward;
    
    @Column(name = "ward_code", length = 20)
    String wardCode;
    
    @Column(length = 100)
    String district;
    
    @Column(name = "district_id")
    Integer districtId;

    @Column(name = "postal_code", length = 20)
    String postalCode;

    @Column(nullable = false, length = 100)
    String country;
    
    @Column(columnDefinition = "TEXT")
    String note;

    @Column(name = "is_default", nullable = false)
    Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
