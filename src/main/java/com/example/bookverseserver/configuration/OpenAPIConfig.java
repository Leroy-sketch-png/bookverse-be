package com.example.bookverseserver.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Bookverse API")
                                                .version("1.0.0")
                                                .description("API documentation for Bookverse Server"))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-key",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
                                .tags(List.of(
                                                new Tag().name("Cart").description("Cart management APIs"),
                                                new Tag().name("Cart Item").description("Cart item management APIs"),
                                                new Tag().name("Shipping Address")
                                                                .description("Shipping address management APIs"),
                                                new Tag().name("Authentication").description("Authentication APIs"),
                                                new Tag().name("Category").description("Category management APIs"),
                                                new Tag().name("Transaction").description("Transaction APIs"),
                                                new Tag().name("Voucher").description(
                                                                "Voucher management APIs - Admin can create/delete vouchers"),
                                                new Tag().name("Review").description(
                                                                "Review & Rating APIs - Users can review books and vote helpful")));
        }
}
