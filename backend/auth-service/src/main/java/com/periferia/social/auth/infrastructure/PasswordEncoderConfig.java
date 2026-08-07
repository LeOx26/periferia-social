package com.periferia.social.auth.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Aparte de SecurityConfig a propósito: el encoder lo necesita el dominio para
 * verificar contraseñas, no la cadena de filtros HTTP. Separarlos permite que
 * SecurityConfig se ocupe solo de qué rutas están protegidas.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
