package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadingLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @ManyToOne @JoinColumn(name = "user_id")
    User user;


    @ManyToOne @JoinColumn(name = "owned_book_id")
    OwnedBook ownedBook;


    Integer currentPage = 0;
    Integer totalPages;
    BigDecimal progressPercent;


    LocalDateTime startedAt;
    LocalDateTime finishedAt;


    @UpdateTimestamp
    LocalDateTime updatedAt;
}