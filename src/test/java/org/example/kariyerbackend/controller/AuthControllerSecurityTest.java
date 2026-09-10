package org.example.kariyerbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kariyerbackend.config.SecurityConfig;
import org.example.kariyerbackend.dto.auth.LoginRequest;
import org.example.kariyerbackend.dto.auth.LoginResponse;
import org.example.kariyerbackend.security.CustomUserDetailsService;
import org.example.kariyerbackend.security.JwtService;
import org.example.kariyerbackend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the httpOnly-cookie auth model wired up in {@link SecurityConfig}: the JWT
 * must never appear in a JS-readable response body, only in a Set-Cookie header, and
 * /api/auth/** must stay reachable without a CSRF token (there is no session yet to forge).
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void login_withoutCsrfToken_setsHttpOnlyCookieAndOmitsTokenFromBody() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse(
                "signed.jwt.token", "Bearer", 1L, "Ada", "Lovelace", "ada@example.com", "USER", null
        ));
        when(jwtService.getExpirationMs()).thenReturn(3_600_000L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ada@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(cookie().value("access_token", "signed.jwt.token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().maxAge("access_token", 3600))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void logout_clearsTheAuthCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0));
    }
}
