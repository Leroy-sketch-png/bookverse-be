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

    /**
     * P0 Security Fix #17: Case-insensitive email/username lookup.
     * Prevents duplicate accounts with different email casing (e.g., User@Gmail.com vs user@gmail.com).
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) OR LOWER(u.username) = LOWER(:username)")
    Optional<User> findByEmailOrUsernameIgnoreCase(@Param("email") String email, @Param("username") String username);

    /**
     * @deprecated Use findByEmailOrUsernameIgnoreCase instead for security.
     * This method is case-sensitive and can lead to duplicate accounts.
     */
    @Deprecated
    Optional<User> findByEmailOrUsername(String email, String username);

    Optional<User> findByGoogleId(String googleId);

    /**
     * P0 Security Fix #17: Case-insensitive email lookup for registration uniqueness check.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * P0 Security Fix #17: Case-insensitive username lookup for registration uniqueness check.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") RoleName roleName, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND (LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search)")
    Page<User> searchUsersByRole(@Param("search") String search, @Param("roleName") RoleName roleName, Pageable pageable);

    /**
     * Count users created within a date range (for trend calculations).
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
