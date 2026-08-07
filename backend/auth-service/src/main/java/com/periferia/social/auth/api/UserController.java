package com.periferia.social.auth.api;

import com.periferia.social.auth.application.GetProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
public class UserController {

    private final GetProfile getProfile;

    public UserController(GetProfile getProfile) {
        this.getProfile = getProfile;
    }

    /** El id del usuario sale del JWT, nunca de un parámetro: así nadie puede pedir el perfil de otro. */
    @GetMapping("/me")
    @Operation(summary = "Devuelve el perfil del usuario autenticado")
    public ProfileResponse me(@AuthenticationPrincipal UUID userId) {
        return ProfileResponse.from(getProfile.execute(userId));
    }
}
