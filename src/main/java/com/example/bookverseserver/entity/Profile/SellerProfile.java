//package com.example.bookverseserver.entity.Profile;
//
//import com.example.bookverseserver.entity.Product.Book;
//import com.example.bookverseserver.entity.User.User;
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.Set;
//
//@Entity
//@Table(name = "seller_profile")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class SellerProfile {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    Long id;
//
//    String shopName;
//    String shopDescription;
//    String contactNumber;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    User user;
//
//    @CreationTimestamp
//    LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    LocalDateTime updatedAt;
//
//    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
//    Set<Book> books;
//}
