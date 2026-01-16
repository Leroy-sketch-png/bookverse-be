package com.example.bookverseserver.configuration;

import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.repository.RoleRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    /**
     * Admin password from environment variable.
     * In production, set APP_ADMIN_PASSWORD environment variable.
     * Default "admin" is ONLY for local development.
     */
    @NonFinal
    @Value("${app.admin.password:admin}")
    String adminPassword;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            // Tạo các role nếu chưa tồn tại
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    roleRepository.save(Role.builder().name(roleName).build());
                    log.info("Created role: {}", roleName);
                }
            });

            // Tạo user admin mặc định nếu chưa có
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);

                // Create admin profile (admins shouldn't go through profile setup)
                UserProfile adminProfile = UserProfile.builder()
                        .fullName("System Administrator")
                        .location("Bookverse HQ")
                        .accountType("BUYER")  // Default, admin doesn't sell
                        .phoneNumber("+1-555-ADMIN")
                        .preferences("fiction,non-fiction,technology")
                        .bio("Platform administrator account")
                        .build();

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .email("admin@bookverse.com")
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .roles(roles)
                        .enabled(true)
                        .userProfile(adminProfile)
                        .build();

                adminProfile.setUser(user);  // Bidirectional relationship

                userRepository.save(user);
                if ("admin".equals(adminPassword)) {
                    log.warn("⚠️ Admin user created with DEFAULT password 'admin' — SET APP_ADMIN_PASSWORD in production!");
                } else {
                    log.info("Admin user created with custom password from environment.");
                }
            }

            log.info("Application initialization completed.");
        };
    }
}
