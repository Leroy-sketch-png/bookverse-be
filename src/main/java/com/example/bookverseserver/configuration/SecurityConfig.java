package com.example.bookverseserver.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.bookverseserver.security.CustomAuthenticationFailureHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/auth/**",
            "/api/author/name/{name}",
            "/api/books",
            "/api/books/**",
            "/error"  // Added error endpoint
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(Customizer.withDefaults());
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(customAuthenticationFailureHandler));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("*"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            log.info("========== JWT DEBUG ==========");
            log.info("JWT Claims: {}", jwt.getClaims());

            Object scopeClaim = jwt.getClaim("scope");
            log.info("Scope claim value: {}", scopeClaim);
            log.info("Scope claim type: {}", scopeClaim != null ? scopeClaim.getClass().getName() : "null");

            Collection<GrantedAuthority> authorities = new ArrayList<>();

            if (scopeClaim == null) {
                log.warn("No scope claim found!");
            } else if (scopeClaim instanceof String) {
                String scope = (String) scopeClaim;
                log.info("Processing scope as String: '{}'", scope);

                // Handle space-separated roles
                String[] roles = scope.split(" ");
                for (String role : roles) {
                    if (!role.trim().isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority(role.trim()));
                        log.info("Added authority: {}", role.trim());
                    }
                }
            } else if (scopeClaim instanceof Collection) {
                log.info("Processing scope as Collection");
                Collection<?> scopes = (Collection<?>) scopeClaim;
                for (Object scope : scopes) {
                    String role = scope.toString();
                    authorities.add(new SimpleGrantedAuthority(role));
                    log.info("Added authority: {}", role);
                }
            } else {
                log.error("Unknown scope type: {}", scopeClaim.getClass());
            }

            log.info("Final authorities: {}", authorities);
            log.info("===============================");

            return authorities;
        });

        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}