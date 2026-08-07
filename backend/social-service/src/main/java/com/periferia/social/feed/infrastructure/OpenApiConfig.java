package com.periferia.social.feed.infrastructure;

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
    OpenAPI socialServiceOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Social Service")
                .version("1.0.0")
                .description("""
                    Publicaciones, likes y difusión en tiempo real.

                    Verifica los JWT emitidos por auth-service con el secreto compartido,
                    sin llamarlo nunca. Los likes se difunden por WebSocket nativo en
                    `ws://localhost:8082/ws/likes?token=<jwt>`, con el mensaje
                    `{"type":"LIKE_UPDATED","postId":"...","likeCount":N}`."""))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
