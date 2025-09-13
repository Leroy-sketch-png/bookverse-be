package com.example.bookverseserver.util;

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

    // common possible claim names
    private static final List<String> CLAIM_KEYS = Arrays.asList(
            "user_id", "uid", "id", "userId", "userid", "sub", "preferred_username", "email", "username"
    );

    public Long getCurrentUserId(Authentication auth) {
        Authentication authentication = auth != null ? auth : SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            // try several claims
            for (String key : CLAIM_KEYS) {
                if (jwt.hasClaim(key)) {
                    Long resolved = parseClaimToIdOrLookup(jwt.getClaim(key), key);
                    if (resolved != null) return resolved;
                }
            }
            // last resort: subject
            Long subjectResolved = parseClaimToIdOrLookup(jwt.getSubject(), "sub");
            if (subjectResolved != null) return subjectResolved;
        }

        if (principal instanceof UserDetails ud) {
            // try username lookup
            return lookupUserIdByUsernameOrEmail(ud.getUsername());
        }

        // fallback: authentication.getName()
        String name = authentication.getName();
        if (name != null) {
            try {
                return Long.parseLong(name);
            } catch (NumberFormatException ignored) {
                Long resolved = lookupUserIdByUsernameOrEmail(name);
                if (resolved != null) return resolved;
            }
        }
        throw new IllegalArgumentException("Unable to determine user id from authentication");
    }

    private Long parseClaimToIdOrLookup(Object claim, String claimKey) {
        if (claim == null) return null;
        if (claim instanceof Number) return ((Number) claim).longValue();
        if (claim instanceof String s) {
            String trimmed = s.trim();
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException ignored) {}
            // try lookup when value is email/username-like
            if (trimmed.contains("@") || claimKey.equalsIgnoreCase("username") || claimKey.equalsIgnoreCase("preferred_username") || claimKey.equalsIgnoreCase("sub")) {
                return lookupUserIdByUsernameOrEmail(trimmed);
            }
        }
        return null;
    }

    private Long lookupUserIdByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null) return null;
        Optional<User> byEmail = userRepository.findByEmail(usernameOrEmail);
        if (byEmail.isPresent()) return byEmail.get().getId();
        //Optional<User> byUsername = userRepository.findByUsername(usernameOrEmail);
        //if (byUsername.isPresent()) return byUsername.get().getId();
        return null;
    }
}
