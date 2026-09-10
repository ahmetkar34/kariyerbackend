package org.example.kariyerbackend.controller;

import org.example.kariyerbackend.config.SecurityConfig;
import org.example.kariyerbackend.dto.job.SavedStatusResponse;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.security.CustomUserDetailsService;
import org.example.kariyerbackend.security.JwtService;
import org.example.kariyerbackend.service.SavedJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the /api/jobs/{id}/favorite matchers added to SecurityConfig for the job
 * favoriting feature - a misplaced matcher here would silently fall through to the
 * broader /api/jobs/** catch-alls (permitAll on GET, hasRole(EMPLOYER) on POST/DELETE),
 * which is exactly the wrong access model for a candidate-only, per-caller resource.
 */
@WebMvcTest(FavoriteController.class)
@Import(SecurityConfig.class)
class FavoriteControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedJobService savedJobService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication authenticationFor(Role role) {
        User user = User.builder().id(1L).firstName("Test").lastName("User").email("test@example.com").role(role).build();
        CustomUserDetails principal = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void getStatus_withoutAuthentication_isRejected() throws Exception {
        mockMvc.perform(get("/api/jobs/5/favorite"))
                .andExpect(status().isForbidden());
    }

    @Test
    void save_withoutCsrfToken_isRejected() throws Exception {
        mockMvc.perform(post("/api/jobs/5/favorite")
                        .with(authentication(authenticationFor(Role.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void save_asEmployer_isForbidden() throws Exception {
        mockMvc.perform(post("/api/jobs/5/favorite")
                        .with(csrf())
                        .with(authentication(authenticationFor(Role.EMPLOYER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void save_asCandidate_isAllowed() throws Exception {
        mockMvc.perform(post("/api/jobs/5/favorite")
                        .with(csrf())
                        .with(authentication(authenticationFor(Role.USER))))
                .andExpect(status().isOk());
    }

    @Test
    void unsave_asCandidate_isAllowed() throws Exception {
        mockMvc.perform(delete("/api/jobs/5/favorite")
                        .with(csrf())
                        .with(authentication(authenticationFor(Role.USER))))
                .andExpect(status().isNoContent());
    }

    @Test
    void getStatus_asCandidate_returnsSavedFlag() throws Exception {
        when(savedJobService.getStatus(eq(5L), eq(1L))).thenReturn(new SavedStatusResponse(true));

        mockMvc.perform(get("/api/jobs/5/favorite")
                        .with(authentication(authenticationFor(Role.USER))))
                .andExpect(status().isOk());
    }
}
