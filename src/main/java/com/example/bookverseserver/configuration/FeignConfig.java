package com.example.bookverseserver.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        // FULL => log toàn bộ header, body, query, response
        return Logger.Level.FULL;
    }
}
