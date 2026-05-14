package com.shopx.product.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

/**
 * @author Sameer Shaikh
 * @date 11-04-2026
 * @description
 */


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "shopx - Product service ", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

  /*  @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Shopx - E-Commerce Application REST API")
                        .version(appVersion)
                        .description(
                                "Secure and scalable REST API services built for a e-commerce platform.\n" +
                                        "Includes modules for Customer, Seller, Product, Cart, and Order management.\n" +
                                        "Session-based authentication system with 1-hour validity for both customers and sellers.\n" +
                                        "Rest apis for all entities with real-time testing via Swagger UI.\n" +
                                        "Built using Spring Boot, Spring Data JPA, Hibernate, and PostgreSQL.\n"+
                                        "\n \t-Created by Sameer Shaikh"+
                                        "\n \t-Tech stack : Code Java (Streams, Exceptions, Builder, Generics), JWT Authentication, Spring Boot(MVC, Security, JPA/Hibernate), PostgreSQL, AWS EC2, Github Actions (CI/CD)"
                        )
                );
    }*/

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Shopx - Product MicroService")
                        .description(
                                        "This Document include product and inventory endpoints."+
                                        "\n \t-Tech stack : Code Java (Streams, Exceptions, Builder, Generics), JWT Authentication, Spring Boot(MVC, Security, JPA/Hibernate), PostgreSQL, AWS EC2, Github Actions (CI/CD)"
                        )
                )
                .servers(List.of(
                        new io.swagger.v3.oas.models.servers.Server().url("/")
                ));
    }

}
