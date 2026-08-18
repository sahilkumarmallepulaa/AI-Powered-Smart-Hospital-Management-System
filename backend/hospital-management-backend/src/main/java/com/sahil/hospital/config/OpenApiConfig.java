package com.sahil.hospital.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AI-Powered Smart Hospital Management System API")
                .version("1.0.0")
                .description("Enterprise REST API documentation for managing hospital workflows, appointments, EHR, and AI integrations.")
                .contact(new Contact()
                    .name("Hospital System Engineering Team")
                    .email("engineering@hospital.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
