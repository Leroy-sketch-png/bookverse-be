package com.example.bookverseserver.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Nhớ import cái này
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.bookverseserver.security.CustomAuthenticationFailureHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List; // Dùng List.of cho gọn

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
            "/api/vouchers/{code}",
            "/api/v1/transactions/**",
            "/error"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(Customizer.withDefaults()); // Kích hoạt CORS config ở dưới

        httpSecurity.authorizeHttpRequests(request ->
                request
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // 👇 QUAN TRỌNG: Cho phép method OPTIONS đi qua mà không cần Token
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/me").authenticated()
                        .anyRequest().authenticated()
        );

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

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-no-retry"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Giữ nguyên logic log của bạn, nó rất tốt để debug
            log.info("========== JWT DEBUG ==========");
            Object scopeClaim = jwt.getClaim("scope");
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            if (scopeClaim instanceof String) {
                String scope = (String) scopeClaim;
                String[] roles = scope.split(" ");
                for (String role : roles) {
                    if (!role.trim().isEmpty()) {
                        // Đảm bảo role có prefix ROLE_ nếu cần, hoặc giữ nguyên tùy logic
                        authorities.add(new SimpleGrantedAuthority(role.trim()));
                    }
                }
            } else if (scopeClaim instanceof Collection) {
                Collection<?> scopes = (Collection<?>) scopeClaim;
                for (Object scope : scopes) {
                    authorities.add(new SimpleGrantedAuthority(scope.toString()));
                }
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}