package com.periferia.social.auth.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    OpenAPI authServiceOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Auth Service")
                .version("1.0.0")
                .description("""
                    Autenticación y perfiles de usuario.

                    Emite JWT firmados con HS256 que social-service verifica en local:
                    los dos servicios comparten el secreto, no se llaman entre sí."""))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
