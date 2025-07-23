package com.example.e_presensi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI usersMicroserviceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        Server httpsServer = new Server()
            .url("https://epresensi-sttp.up.railway.app")
            .description("Production Server (HTTPS)");
        return new OpenAPI()
                .addServersItem(httpsServer)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new Info()
                        .title("E-Presensi API")
                        .description("API Documentation untuk E-Presensi")
                        .version("1.0"));
    }
}