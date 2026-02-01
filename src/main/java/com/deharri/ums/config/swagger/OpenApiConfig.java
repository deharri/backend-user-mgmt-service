package com.deharri.ums.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration for User Management Service.
 * <p>
 * This configuration provides comprehensive API documentation including:
 * <ul>
 *     <li>API metadata (title, version, description)</li>
 *     <li>Security scheme (JWT Bearer authentication)</li>
 *     <li>Server information for different environments</li>
 *     <li>API tags for endpoint grouping</li>
 * </ul>
 *
 * @author Ali Haris Chishti
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(externalDocumentation())
                .servers(serverList())
                .tags(apiTags())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private Info apiInfo() {
        return new Info()
                .title("User Management Service API")
                .version("1.0.0")
                .description("""
                        ## Overview
                        
                        The **User Management Service API** provides a comprehensive set of endpoints for managing 
                        user authentication, authorization, and profile management.
                        
                        ### Features
                        - 🔐 **Authentication**: Register, login, logout, and token refresh
                        - 👤 **User Profile**: View and update user profiles
                        - 📧 **Email Management**: Update and verify email addresses
                        - 📱 **Phone Management**: Update and verify phone numbers
                        - 🔑 **Password Management**: Secure password updates
                        - 🖼️ **Profile Pictures**: Upload and retrieve profile images via S3
                        
                        ### Authentication
                        This API uses **JWT Bearer Token** authentication. Include the token in the 
                        `Authorization` header: `Bearer <your-token>`
                        
                        ### Rate Limiting
                        API requests are rate-limited to ensure fair usage. Default limits:
                        - Authentication endpoints: 10 requests/minute
                        - Profile endpoints: 60 requests/minute
                        """)
                .contact(new Contact()
                        .name("Deharri Development Team")
                        .email("harischishti28@outlook.com")
                        .url("https://github.com/deharri"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"))
                .termsOfService("https://deharri.com/terms");
    }

    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation()
                .description("User Management Service Documentation")
                .url("https://github.com/deharri/backend-user-mgmt-service/wiki");
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("http://localhost:8081")
                        .description("Docker Development Server"),
                new Server()
                        .url("https://api.deharri.com/ums")
                        .description("Production Server")
        );
    }

    private List<Tag> apiTags() {
        return List.of(
                new Tag()
                        .name("Authentication")
                        .description("Endpoints for user authentication including registration, login, logout, and token management"),
                new Tag()
                        .name("User Profile")
                        .description("Endpoints for viewing and managing user profile information"),
                new Tag()
                        .name("Password Management")
                        .description("Endpoints for password-related operations"),
                new Tag()
                        .name("Contact Information")
                        .description("Endpoints for managing email and phone number"),
                new Tag()
                        .name("Profile Picture")
                        .description("Endpoints for uploading and retrieving user profile pictures")
        );
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT Bearer token authentication.
                                        
                                        To obtain a token:
                                        1. Register a new account via `/api/v1/auth/register`
                                        2. Or login with existing credentials via `/api/v1/auth/login`
                                        
                                        Include the token in requests: `Authorization: Bearer <token>`
                                        """)
                );
    }
}
