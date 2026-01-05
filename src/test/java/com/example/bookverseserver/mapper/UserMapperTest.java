//package com.example.bookverseserver.mapper;
//
//import com.example.bookverseserver.dto.request.User.UserCreationRequest;
//import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
//import com.example.bookverseserver.dto.response.User.UserResponse;
//import com.example.bookverseserver.entity.User.Role;
//import com.example.bookverseserver.entity.User.User;
//import com.example.bookverseserver.enums.RoleName;
//import com.example.bookverseserver.repository.RoleRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//class UserMapperTest {
//
//    @Autowired
//    private UserMapper userMapper;
//
//    @MockBean
//    private RoleRepository roleRepository;
//
//    @BeforeEach
//    void setUp() {
//        Role userRole = new Role();
//        userRole.setName(RoleName.BUYER);
//        when(roleRepository.findByName(RoleName.BUYER)).thenReturn(Optional.of(userRole));
//
//        Role adminRole = new Role();
//        adminRole.setName(RoleName.ADMIN);
//        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
//    }
//
//    @Test
//    void testToUser() {
//        UserCreationRequest request = new UserCreationRequest();
//        request.setUsername("testuser");
//        request.setEmail("test@example.com");
//
//        User user = userMapper.toUser(request);
//
//        assertThat(user).isNotNull();
//        assertThat(user.getUsername()).isEqualTo("testuser");
//        assertThat(user.getEmail()).isEqualTo("test@example.com");
//    }
//
//    @Test
//    void testToUserResponse() {
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("testuser");
//        user.setEmail("test@example.com");
//
//        Role userRole = new Role();
//        userRole.setName(RoleName.BUYER);
//        user.setRoles(Set.of(userRole));
//
//        UserResponse response = userMapper.toUserResponse(user);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getId()).isEqualTo(1L);
//        assertThat(response.getUsername()).isEqualTo("testuser");
//        assertThat(response.getEmail()).isEqualTo("test@example.com");
//        assertThat(response.getRoles()).hasSize(1);
//        assertThat(response.getRoles().iterator().next().getName()).isEqualTo(RoleName.BUYER);
//    }
//
//    @Test
//    void testUpdateUser() {
//        UserUpdateRequest request = new UserUpdateRequest();
//        request.setRoles(List.of("ADMIN"));
//
//        User user = new User();
//        user.setUsername("testuser");
//        Role userRole = new Role();
//        userRole.setName(RoleName.BUYER);
//        user.setRoles(Set.of(userRole));
//
//        userMapper.updateUser(user, request);
//
//        assertThat(user.getRoles()).hasSize(1);
//        assertThat(user.getRoles().iterator().next().getName()).isEqualTo(RoleName.ADMIN);
//    }
//}
