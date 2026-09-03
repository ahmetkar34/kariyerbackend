package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.auth.LoginRequest;
import org.example.kariyerbackend.dto.auth.LoginResponse;
import org.example.kariyerbackend.dto.auth.RegisterRequest;
import org.example.kariyerbackend.dto.auth.RegisterResponse;
import org.example.kariyerbackend.entity.EmployerProfile;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.EmployerProfileRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployerProfileRepository employerProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_asCandidate_doesNotCreateEmployerProfile() {
        RegisterRequest request = new RegisterRequest(
                "Ali", "Veli", "ali@test.com", "password123", true, "candidate", null
        );
        when(userRepository.existsByEmail("ali@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        RegisterResponse response = authService.register(request);

        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.companyName()).isNull();
        verify(employerProfileRepository, never()).save(any());
    }

    @Test
    void register_asEmployer_createsEmployerProfileWithCompanyName() {
        RegisterRequest request = new RegisterRequest(
                "Emp", "Owner", "emp@test.com", "password123", true, "employer", "Acme Inc"
        );
        when(userRepository.existsByEmail("emp@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });
        when(employerProfileRepository.save(any(EmployerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.register(request);

        assertThat(response.role()).isEqualTo(Role.EMPLOYER);
        assertThat(response.companyName()).isEqualTo("Acme Inc");
    }

    @Test
    void register_employerWithoutCompanyName_throwsBadRequest() {
        RegisterRequest request = new RegisterRequest(
                "Emp", "Owner", "emp2@test.com", "password123", true, "employer", null
        );
        when(userRepository.existsByEmail("emp2@test.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest(
                "Ali", "Veli", "ali@test.com", "password123", true, "candidate", null
        );
        when(userRepository.existsByEmail("ali@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_candidate_returnsTokenWithoutCompanyName() {
        User user = User.builder()
                .id(3L).firstName("Ali").lastName("Veli").email("ali@test.com").role(Role.USER)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("ali@test.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.companyName()).isNull();
    }

    @Test
    void login_employer_includesCompanyName() {
        User user = User.builder()
                .id(4L).firstName("Emp").lastName("Owner").email("emp@test.com").role(Role.EMPLOYER)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(employerProfileRepository.findById(4L))
                .thenReturn(Optional.of(EmployerProfile.builder().userId(4L).companyName("Acme").build()));

        LoginResponse response = authService.login(new LoginRequest("emp@test.com", "password123"));

        assertThat(response.companyName()).isEqualTo("Acme");
    }

    @Test
    void login_badCredentials_throwsUnauthorized() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("ali@test.com", "wrong")));

        assertThat(ex.getStatusCode().value()).isEqualTo(401);
        verifyNoInteractions(jwtService);
    }
}
