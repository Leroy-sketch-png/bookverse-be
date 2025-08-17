//package com.example.bookverseserver.entity.Profile;
//
//import com.example.bookverseserver.entity.User.User;
//import com.example.bookverseserver.enums.MembershipType;
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "customer_profile")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class CustomerProfile {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    Long id;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false, unique = true)
//    User user;
//
//    String fullName;
//    String phone;
//    LocalDate dob;
//
//    @Enumerated(EnumType.STRING)
//    MembershipType mbsType;
//
//    String avatarUrl;
//    String bio;
//
//    @CreationTimestamp
//    LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    LocalDateTime updatedAt;
//
//
//}
