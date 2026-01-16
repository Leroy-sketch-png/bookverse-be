package com.example.bookverseserver.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Web configuration for the Bookverse application.
 * 
 * Security: Caps pagination size to prevent DoS attacks via ?size=999999
 */
@Configuration
@EnableSpringDataWebSupport
public class WebConfig implements WebMvcConfigurer {

    /**
     * Maximum allowed page size to prevent memory exhaustion attacks.
     * Any request with size > MAX_PAGE_SIZE will be capped to MAX_PAGE_SIZE.
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Default page size when not specified.
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Customizes the Pageable resolver to enforce safe pagination limits.
     * This applies globally to all endpoints using Spring Data's Pageable.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(MAX_PAGE_SIZE);
        resolver.setFallbackPageable(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        resolvers.add(resolver);
    }
}
