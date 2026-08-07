package com.periferia.social.feed.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            // Toma el CorsConfigurationSource declarado en CorsConfig. Sin esta línea
            // el navegador bloquea toda respuesta: el front vive en otro puerto.
            .cors(org.springframework.security.config.Customizer.withDefaults())
            // Toma el CorsConfigurationSource de CorsConfig. Sin esta línea el
            // navegador bloquea toda respuesta: el front vive en otro puerto.
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // /ws/** queda fuera de la cadena HTTP: el handshake del WebSocket
                // se autentica con su propio interceptor, porque el navegador no
                // permite enviar cabeceras al abrir la conexión.
                .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**",
                                 "/actuator/**", "/ws/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
