package com.example.bookverseserver.entity.User;

import com.example.bookverseserver.entity.Product.Book;
import com.example.bookverseserver.entity.Product.Listing;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnedBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @ManyToOne @JoinColumn(name = "user_id")
    User user;


    @ManyToOne @JoinColumn(name = "book_id")
    Book book;


    @ManyToOne @JoinColumn(name = "listing_id")
    Listing listing;


    LocalDateTime acquiredAt;
    @Enumerated(EnumType.STRING)
    BookCondition condition;
    Boolean isGift = false;
    String notes;


    @CreationTimestamp
    LocalDateTime createdAt;
}