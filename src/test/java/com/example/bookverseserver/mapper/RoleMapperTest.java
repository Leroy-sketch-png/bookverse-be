package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Authentication.RoleRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMapperTest {

    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RoleMapperImpl();
    }

    @Test
    void testToRole() {
        RoleRequest request = new RoleRequest();
        request.setName(RoleName.ADMIN);

        Role role = roleMapper.toRole(request);

        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo(RoleName.ADMIN);
    }

    @Test
    void testToRoleResponse() {
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleName.ADMIN);

        RoleResponse response = roleMapper.toRoleResponse(role);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(RoleName.ADMIN);
    }
}
