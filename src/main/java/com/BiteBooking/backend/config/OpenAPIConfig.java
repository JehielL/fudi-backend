package com.BiteBooking.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BiteBooking API")
                        .version("1.0.0")
                        .description("API REST para el sistema de reservas de restaurantes BiteBooking. " +
                                "Esta API permite gestionar usuarios, restaurantes, menús, platos, reservas y calificaciones con imágenes.")
                        .contact(new Contact()
                                .name("BiteBooking Team")
                                .email("support@bitebooking.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo"),
                        new Server()
                                .url("https://api.bitebooking.com")
                                .description("Servidor de producción")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Autenticación mediante JWT. Obtén el token desde /users/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
