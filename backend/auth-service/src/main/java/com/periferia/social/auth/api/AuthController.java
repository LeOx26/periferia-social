package com.periferia.social.auth.api;

import com.periferia.social.auth.application.AuthenticateUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Emisión de tokens de acceso")
public class AuthController {

    private final AuthenticateUser authenticateUser;

    public AuthController(AuthenticateUser authenticateUser) {
        this.authenticateUser = authenticateUser;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un JWT firmado con HS256")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUser.execute(request.username(), request.password());
        return LoginResponse.from(result.token(), result.expiresInSeconds(), result.user());
    }
}
