package com.periferia.social.auth.api;

import com.periferia.social.auth.application.AuthenticateUser;
import com.periferia.social.auth.domain.InvalidCredentialsException;
import com.periferia.social.auth.domain.User;
import com.periferia.social.auth.infrastructure.JwtAuthenticationFilter;
import com.periferia.social.auth.infrastructure.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Se importa el JwtAuthenticationFilter real en lugar de mockearlo: un mock de un
 * filtro no invoca chain.doFilter(), así que corta la cadena y el controlador nunca
 * se ejecuta. Con el filtro real y sin cabecera Authorization, la petición
 * simplemente pasa de largo, que es lo que necesita /api/auth/login.
 */
@WebMvcTest(
    controllers = AuthController.class,
    properties = "security.jwt.secret=un-secreto-de-desarrollo-de-al-menos-32-bytes!"
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthenticateUser authenticateUser;

    @Test
    void returns_a_token_on_successful_login() throws Exception {
        User leo = new User(UUID.randomUUID(), "leo", "hash", "Leonel", "Benítez",
                            LocalDate.of(1993, 4, 12), "leo");
        when(authenticateUser.execute(any(), any()))
            .thenReturn(new AuthenticateUser.AuthResult("un.token.jwt", 3600, leo));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"leo","password":"Periferia2026!"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("un.token.jwt"))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(jsonPath("$.user.alias").value("leo"));
    }

    @Test
    void returns_401_as_problem_details_on_bad_credentials() throws Exception {
        when(authenticateUser.execute(any(), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"leo","password":"mala"}"""))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.title").value("Usuario o contraseña incorrectos"))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void returns_400_when_the_username_is_blank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"","password":"algo"}"""))
            .andExpect(status().isBadRequest());
    }
}
