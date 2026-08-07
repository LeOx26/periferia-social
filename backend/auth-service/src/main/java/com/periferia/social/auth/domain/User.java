package com.periferia.social.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected User() { /* requerido por JPA */ }

    public User(UUID id, String username, String passwordHash, String firstName,
                String lastName, LocalDate birthDate, String alias) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.alias = alias;
    }

    /** El hash nunca sale del agregado; la comparación ocurre dentro. */
    public boolean matchesPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, passwordHash);
    }

    public UUID id() { return id; }
    public String username() { return username; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public LocalDate birthDate() { return birthDate; }
    public String alias() { return alias; }

    @Override
    public String toString() {
        return "User[id=%s, username=%s, alias=%s]".formatted(id, username, alias);
    }
}
