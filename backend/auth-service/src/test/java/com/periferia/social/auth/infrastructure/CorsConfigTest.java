package com.periferia.social.auth.infrastructure;

import com.periferia.social.auth.api.AuthController;
import com.periferia.social.auth.application.AuthenticateUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Este test existe porque la ausencia de CORS es invisible desde curl: solo el
 * navegador aplica la política, así que la API respondía 200 sin las cabeceras y
 * la aplicación web fallaba silenciosamente al no poder leer la respuesta.
 */
@WebMvcTest(
    controllers = AuthController.class,
    properties = {
        "security.jwt.secret=un-secreto-de-desarrollo-de-al-menos-32-bytes!",
        "security.cors.allowed-origins=http://localhost:5173,http://localhost:5174",
    }
)
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class})
class CorsConfigTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthenticateUser authenticateUser;

    @Test
    void answers_the_preflight_the_browser_sends_before_logging_in() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,x-correlation-id"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void allows_the_containerised_expo_web_origin_too() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:5174")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }

    @Test
    void rejects_an_origin_that_was_not_declared() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://sitio-malicioso.example")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isForbidden());
    }
}
