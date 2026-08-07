package com.periferia.social.auth.application;

import com.periferia.social.auth.domain.InvalidCredentialsException;
import com.periferia.social.auth.domain.User;
import com.periferia.social.auth.domain.UserRepository;
import com.periferia.social.auth.infrastructure.JwtIssuer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUser {

    public record AuthResult(String token, long expiresInSeconds, User user) {}

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtIssuer issuer;

    public AuthenticateUser(UserRepository repository, PasswordEncoder encoder, JwtIssuer issuer) {
        this.repository = repository;
        this.encoder = encoder;
        this.issuer = issuer;
    }

    public AuthResult execute(String username, String rawPassword) {
        User user = repository.findByUsername(username)
            .orElseThrow(InvalidCredentialsException::new);

        if (!user.matchesPassword(rawPassword, encoder)) {
            throw new InvalidCredentialsException();
        }

        return new AuthResult(issuer.issue(user), issuer.expiresInSeconds(), user);
    }
}
