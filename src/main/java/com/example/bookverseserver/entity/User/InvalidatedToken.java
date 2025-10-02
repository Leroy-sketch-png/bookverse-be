package com.example.bookverseserver.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

/**
 * Entity representing an invalidated JWT token (used for blacklisting/revocation).
 * The primary key (id) is manually assigned from the JWT's JTI claim (a String UUID),
 * thus no @GeneratedValue is used.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invalidated_token")
public class InvalidatedToken {

    @Id
    String id;

    // We explicitly name the column "expires_at".
    // We use @Temporal(TemporalType.TIMESTAMP) for proper mapping of java.util.Date.
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expires_at", nullable = false)
    Date expiryTime;

    /**
     * Field to satisfy the database's NOT NULL constraint on 'created_at'.
     * It is set automatically before the entity is saved (persisted).
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    /**
     * JPA callback method executed before the entity is persisted (inserted).
     * This automatically sets the current date for the 'created_at' column,
     * resolving the DataIntegrityViolationException.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }
}
