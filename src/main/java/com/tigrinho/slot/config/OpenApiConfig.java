package com.tigrinho.slot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * This class sets up the API documentation details, security schemes,
 * and defines different API groups for better organization.
 */
@Configuration
public class OpenApiConfig {

    @Value("${tigrinho.app.version:1.0.0}")
    private String appVersion;

    @Value("${tigrinho.app.description:REST API for Tigrinho Slot Game}")
    private String appDescription;

    /**
     * Configures the main OpenAPI bean with global information,
     * server details, and security schemes.
     *
     * @return A customized {@link OpenAPI} object.
     */
    @Bean
    public OpenAPI tigrinhoOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.tigrinho.com").description("Production Server")
                ))
                .info(new Info()
                        .title("Tigrinho Slot Game API")
                        .description(appDescription)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("Tigrinho Support")
                                .email("support@tigrinho.com")
                                .url("https://tigrinho.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                        .termsOfService("https://tigrinho.com/terms"));
    }

    /**
     * Defines a grouped API for public endpoints.
     * Paths matching "/api/public/**" will be included in this group.
     *
     * @return A {@link GroupedOpenApi} instance for public APIs.
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("tigrinho-public")
                .pathsToMatch("/api/public/**")
                .build();
    }

    /**
     * Defines a grouped API for admin-specific endpoints.
     * Paths matching "/api/admin/**" will be included in this group,
     * with a custom info section for the admin API.
     *
     * @return A {@link GroupedOpenApi} instance for admin APIs.
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("tigrinho-admin")
                .pathsToMatch("/api/admin/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info().title("Tigrinho Admin API")
                                .description("Admin REST API for Tigrinho Slot Game")
                                .version(appVersion)
                                .contact(new Contact()
                                        .name("Tigrinho Support")
                                        .email("support@tigrinho.com")
                                        .url("https://tigrinho.com/support"))
                                .license(new License()
                                        .name("MIT License")
                                        .url("https://opensource.org/licenses/MIT"))
                ))
                .build();
    }
}
