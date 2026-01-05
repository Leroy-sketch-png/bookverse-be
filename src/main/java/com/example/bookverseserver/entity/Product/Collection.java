package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "collection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "collection_listing",
        joinColumns = @JoinColumn(name = "collection_id"),
        inverseJoinColumns = @JoinColumn(name = "listing_id")
    )
    @Builder.Default
    Set<Listing> listings = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    // Helper method to get total books count
    public Integer getTotalBooks() {
        return listings != null ? listings.size() : 0;
    }
}
