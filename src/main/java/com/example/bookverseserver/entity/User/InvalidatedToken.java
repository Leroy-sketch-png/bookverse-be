package com.example.bookverseserver.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.Date;

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
    @Column(name = "jti", nullable = false, length = 255)
    String id;

    @Column(name = "expires_at", nullable = false)
    Date expiryTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;
}
