package com.periferia.social.auth.infrastructure;

import com.periferia.social.auth.domain.User;
import com.periferia.social.auth.domain.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Adaptador de persistencia. Spring Data genera la implementación en tiempo de
 * ejecución; extender el puerto de dominio mantiene a AuthenticateUser ignorante
 * de que detrás hay JPA.
 */
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {
}
