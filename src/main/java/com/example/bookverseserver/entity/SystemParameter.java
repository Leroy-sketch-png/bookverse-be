package com.example.bookverseserver.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemParameter {
    @Id
    String key;


    String value;
    String description;
    String scope;


    @UpdateTimestamp
    LocalDateTime updatedAt;
}