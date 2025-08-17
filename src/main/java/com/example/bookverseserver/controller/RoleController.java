package com.example.bookverseserver.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.request.Authentication.RoleRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

//    @DeleteMapping("/{role}")
//    ApiResponse<Void> delete(@PathVariable String role) {
//        roleService.delete(role);
//        return ApiResponse.<Void>builder().build();
//    }
}