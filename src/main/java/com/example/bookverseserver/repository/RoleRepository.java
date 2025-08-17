package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);

    List<Role> findAllByNameIn(List<RoleName> names);
}
