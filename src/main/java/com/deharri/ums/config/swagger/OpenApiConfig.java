package com.deharri.ums.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management Service API")
                        .version("v1")
                        .description("API documentation for the User Management Service")
                        .contact(new Contact()
                                .name("Ali Haris Chishti")
                                .email("harischishti28@outlook.com")
                                .url("https://github.com/deharri/backend-user-mgmt-service")));
    }
}
