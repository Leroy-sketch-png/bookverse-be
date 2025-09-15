package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "\"user\"") // Match lowercase table name as in schema
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 50, nullable = false, unique = true)
    String username;

    @Column(unique = true, nullable = false, length = 100)
    @Email
    String email;

    @Column(name = "password_hash", length = 255)
    String passwordHash;

    @Column(nullable = false)
    @lombok.Builder.Default
    Boolean enabled = true;

    @Column(name = "last_login")
    LocalDateTime lastLogin;

    @Column(name = "failed_login_count", nullable = false)
    @lombok.Builder.Default
    Integer failedLoginCount = 0;

    @Column(name = "locked_until")
    LocalDateTime lockedUntil;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    String adminNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    UserProfile userProfile;

    @Column(unique = true)
    String googleId;

    String authProvider;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles;
}
