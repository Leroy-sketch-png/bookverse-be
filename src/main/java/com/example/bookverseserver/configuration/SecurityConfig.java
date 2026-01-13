package com.example.bookverseserver.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    
    // CORS origins from application.properties (no more hardcoding!)
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;
    
    // Public endpoints for unauthenticated access (GET only for most)
    // Note: POST /api/books/from-open-library requires auth (handled by @SecurityRequirement)
    private final String[] PUBLIC_GET_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/auth/**",
            "/api/public/**",        // Public stats for homepage
            "/api/author",           // GET all authors for browsing
            "/api/author/{id}",      // GET single author
            "/api/author/name/{name}",
            "/api/categories",       // GET categories is public for browsing
            "/api/categories/{id}",  // GET single category
            "/api/vouchers/{code}",
            "/api/v1/transactions/**",
            "/error"
    };
    
    // Stripe webhook endpoint - must be public for Stripe to call it
    // Security is handled via Stripe signature verification in the controller
    private final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/stripe/webhook"    // Stripe webhook (signature verified in controller)
    };
    
    // Books endpoints - GET is public, POST requires auth
    private final String[] PUBLIC_BOOKS_GET = {
            "/api/books",       // GET all books
            "/api/books/{id}",  // GET book by ID
            "/api/books/lookup/{isbn}"  // GET book preview by ISBN (no auth, no DB write)
    };
    
    // Listings endpoints - GET is public for browsing marketplace
    private final String[] PUBLIC_LISTINGS_GET = {
            "/api/listings",           // GET all listings (browse marketplace)
            "/api/listings/{id}",      // GET single listing detail
            "/api/listings/{id}/reviews",  // GET reviews for a listing
            "/api/listings/seller/{sellerId}"  // GET seller's listings (public browsing)
    };
    
    // Seller public profile - visible to anyone browsing (per Vision API_CONTRACTS.md §7.4)
    private final String[] PUBLIC_SELLERS_GET = {
            "/api/seller/{sellerSlug}/profile",           // GET seller public profile
            "/api/seller/{sellerSlug}/profile/listings",  // GET seller's listings
            "/api/seller/{sellerSlug}/profile/reviews"    // GET seller's reviews
    };
    
    // Shipping endpoints - public for rate calculation and address lookup
    private final String[] PUBLIC_SHIPPING_GET = {
            "/api/shipping/calculate",       // GET shipping rate calculation
            "/api/shipping/services",        // GET available shipping services
            "/api/shipping/track/**",        // GET tracking info
            "/api/shipping/provinces",       // GET provinces list
            "/api/shipping/provinces/*/districts",  // GET districts by province
            "/api/shipping/districts/*/wards"       // GET wards by district
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
                        .requestMatchers(PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_BOOKS_GET).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_LISTINGS_GET).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_SELLERS_GET).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_SHIPPING_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()

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

        // Parse comma-separated origins from config (no more hardcoded localhost!)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        log.info("CORS configured with allowed origins: {}", origins);

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