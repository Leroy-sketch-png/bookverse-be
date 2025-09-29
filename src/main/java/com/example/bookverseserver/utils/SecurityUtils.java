package com.example.bookverseserver.utils;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public Long getCurrentUserId(Authentication auth) {
        Authentication authentication = auth != null ? auth : SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            try {
                return Long.parseLong(jwt.getSubject()); // always user.id
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid subject in JWT");
            }
        }

        if (principal instanceof UserDetails ud) {
            return lookupUserIdByUsernameOrEmail(ud.getUsername());
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return lookupUserIdByUsernameOrEmail(authentication.getName());
        }
    }

    private Long lookupUserIdByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null) return null;
        return userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for: " + usernameOrEmail));
    }
}
