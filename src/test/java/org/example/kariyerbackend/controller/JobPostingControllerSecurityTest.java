package org.example.kariyerbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kariyerbackend.config.SecurityConfig;
import org.example.kariyerbackend.dto.common.PageResponse;
import org.example.kariyerbackend.dto.job.JobPostingRequest;
import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.security.CustomUserDetailsService;
import org.example.kariyerbackend.security.JwtService;
import org.example.kariyerbackend.service.JobPostingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the role/method matchers wired up in {@link SecurityConfig} through the real
 * security filter chain, instead of trusting them by inspection. A misconfigured matcher
 * here (e.g. an accidental permitAll on a write endpoint) would previously go unnoticed
 * until manual testing.
 */
@WebMvcTest(JobPostingController.class)
@Import(SecurityConfig.class)
class JobPostingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JobPostingService jobPostingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private JobPostingRequest sampleRequest() {
        return new JobPostingRequest(
                "Backend Developer", "Acme", "Istanbul", "Tam Zamanlı", true, "50000",
                List.of("Java"), "description", List.of(), List.of(), "about"
        );
    }

    private Authentication authenticationFor(Role role) {
        User user = User.builder().id(1L).firstName("Test").lastName("User").email("test@example.com").role(role).build();
        CustomUserDetails principal = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void listingJobs_isPubliclyAccessible() throws Exception {
        when(jobPostingService.search(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void creatingJob_withoutAuthentication_isRejected() throws Exception {
        // No AuthenticationEntryPoint is configured in SecurityConfig, so Spring Security's
        // default (Http403ForbiddenEntryPoint) responds 403 rather than 401 for anonymous requests.
        // A valid CSRF token is supplied so this failure is attributable to authentication,
        // not incidentally masked by the CSRF check that now also guards this endpoint.
        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatingJob_asCandidate_isForbidden() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .with(authentication(authenticationFor(Role.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatingJob_asEmployerWithoutCsrfToken_isRejected() throws Exception {
        // /api/jobs is a mutating endpoint outside the /api/auth/** CSRF exemption, so it
        // must reject a request that lacks a valid CSRF token even from a legitimate,
        // correctly-authorized caller.
        mockMvc.perform(post("/api/jobs")
                        .with(authentication(authenticationFor(Role.EMPLOYER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatingJob_asEmployer_isAllowed() throws Exception {
        when(jobPostingService.create(eq(1L), any())).thenReturn(new JobPostingResponse(
                1L, 1L, "Backend Developer", "Acme", "Istanbul", "Tam Zamanlı", true, "50000",
                List.of("Java"), "description", List.of(), List.of(), "about", 0L, LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .with(authentication(authenticationFor(Role.EMPLOYER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated());
    }
}
