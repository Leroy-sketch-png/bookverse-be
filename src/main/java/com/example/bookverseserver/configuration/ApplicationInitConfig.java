package com.example.bookverseserver.configuration;

import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.repository.RoleRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

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

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .email("admin@bookverse.com")
                        .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(adminRole) // chỉ 1 role duy nhất
                        .enabled(true)
                        .build();

                userRepository.save(user);
                log.warn("Admin user created with default password: 'admin' — please change it.");
            }

            log.info("Application initialization completed.");
        };
    }
}
