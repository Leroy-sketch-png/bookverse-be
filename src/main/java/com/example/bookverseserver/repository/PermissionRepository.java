package com.example.bookverseserver.repository;

import com.example.bookverseserver.dto.request.Authentication.PermissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookverseserver.entity.User.Permission;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
