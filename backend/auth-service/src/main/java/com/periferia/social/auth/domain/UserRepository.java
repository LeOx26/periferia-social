package com.periferia.social.auth.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia definido por el dominio. El adaptador vive en
 * infrastructure, de modo que el dominio no depende de JPA ni de Spring Data.
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);
}
