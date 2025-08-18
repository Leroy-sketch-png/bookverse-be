package com.example.bookverseserver.entity.User;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorId implements Serializable {
    Long userId;
    Long authorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAuthorId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(authorId, that.authorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, authorId);
    }
}
