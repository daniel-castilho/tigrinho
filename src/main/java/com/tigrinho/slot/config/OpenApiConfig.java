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

@Configuration
public class OpenApiConfig {

    @Value("${tigrinho.app.version:1.0.0}")
    private String appVersion;

    @Value("${tigrinho.app.description:REST API for Tigrinho Slot Game}")
    private String appDescription;

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

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("tigrinho-public")
                .pathsToMatch("/api/public/**")
                .build();
    }

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
