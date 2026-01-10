package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrUsername(String email, String username);

    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") RoleName roleName, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND (LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search)")
    Page<User> searchUsersByRole(@Param("search") String search, @Param("roleName") RoleName roleName, Pageable pageable);
}
