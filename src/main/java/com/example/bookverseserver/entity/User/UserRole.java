//package com.example.bookverseserver.entity.User;
//
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//@Entity
//@Table(name = "user_role")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class UserRole {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("userId")
//    @JoinColumn(name = "user_id")
//    User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("roleId")
//    @JoinColumn(name = "role_id")
//    Role role;
//
//    // Optional: thêm thông tin về thời gian gán role
//    // @CreationTimestamp
//    // LocalDateTime assignedAt;
//}
