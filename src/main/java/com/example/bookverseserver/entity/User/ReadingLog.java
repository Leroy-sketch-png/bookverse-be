package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "reading_log")
public class ReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owned_book_id", nullable = false)
    OwnedBook ownedBook;

    @Column(name = "current_page", nullable = false)
    Integer currentPage = 0;

    @Column(name = "total_pages")
    Integer totalPages;

    @Column(name = "started_at")
    LocalDateTime startedAt;

    @Column(name = "finished_at")
    LocalDateTime finishedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    // Map the DB-generated column progress_percent (read-only in JPA)
    // DB should compute it as (current_page / NULLIF(total_pages,0)) * 100
    @Column(name = "progress_percent", insertable = false, updatable = false, precision = 5, scale = 2)
    BigDecimal progressPercent;
}
