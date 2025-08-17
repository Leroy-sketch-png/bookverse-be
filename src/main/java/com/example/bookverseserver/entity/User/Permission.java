package com.example.bookverseserver.entity.User;

import com.example.bookverseserver.enums.PermissionName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 50)
    PermissionName name;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    Set<Role> roles;
}
