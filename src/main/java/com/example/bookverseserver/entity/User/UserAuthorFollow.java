package com.example.bookverseserver.entity.User;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.entity.User.UserAuthorId;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "userauthorfollow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAuthorFollow {

    @EmbeddedId
    UserAuthorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // maps userId from composite key
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId") // maps authorId from composite key
    @JoinColumn(name = "author_id", nullable = false)
    Author author;

    @CreationTimestamp
    LocalDateTime followedAt;
}
