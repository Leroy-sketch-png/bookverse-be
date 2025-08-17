package com.example.bookverseserver.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bookverseserver.dto.request.Authentication.PermissionRequest;
import com.example.bookverseserver.dto.response.Authentication.PermissionResponse;
import com.example.bookverseserver.entity.User.Permission;
import com.example.bookverseserver.mapper.PermissionMapper;
import com.example.bookverseserver.repository.PermissionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}