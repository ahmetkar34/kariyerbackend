package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.auth.LoginRequest;
import org.example.kariyerbackend.dto.auth.LoginResponse;
import org.example.kariyerbackend.dto.auth.RegisterRequest;
import org.example.kariyerbackend.dto.auth.RegisterResponse;
import org.example.kariyerbackend.entity.EmployerProfile;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.TokenPurpose;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.entity.VerificationToken;
import org.example.kariyerbackend.repository.EmployerProfileRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.example.kariyerbackend.repository.VerificationTokenRepository;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private EmailService emailService;
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
        verify(emailService).sendVerificationEmail(org.mockito.ArgumentMatchers.eq("ali@test.com"), any());
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
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

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
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
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

    @Test
    void login_unverifiedEmail_throwsForbidden() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("ali@test.com", "password123")));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        verifyNoInteractions(jwtService);
    }

    @Test
    void verifyEmailCode_validCode_marksUserVerifiedAndLogsIn() {
        User user = User.builder().id(5L).email("ali@test.com").firstName("Ali").lastName("Veli")
                .role(Role.USER).emailVerified(false).build();
        VerificationToken token = VerificationToken.builder()
                .token("123456").userId(5L).purpose(TokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).build();
        when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        LoginResponse response = authService.verifyEmailCode("ali@test.com", "123456");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("ali@test.com");
    }

    @Test
    void verifyEmailCode_unknownEmail_throwsBadRequest() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmailCode("missing@test.com", "123456"));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(verificationTokenRepository);
    }

    @Test
    void verifyEmailCode_codeBelongsToDifferentUser_throwsBadRequest() {
        User user = User.builder().id(5L).email("ali@test.com").build();
        VerificationToken token = VerificationToken.builder()
                .token("123456").userId(999L).purpose(TokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).build();
        when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmailCode("ali@test.com", "123456"));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void verifyEmailCode_expiredCode_throwsBadRequest() {
        User user = User.builder().id(5L).email("ali@test.com").build();
        VerificationToken token = VerificationToken.builder()
                .token("123456").userId(5L).purpose(TokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().minusMinutes(1)).build();
        when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmailCode("ali@test.com", "123456"));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void verifyEmailCode_alreadyUsedCode_throwsBadRequest() {
        User user = User.builder().id(5L).email("ali@test.com").build();
        VerificationToken token = VerificationToken.builder()
                .token("123456").userId(5L).purpose(TokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).usedAt(LocalDateTime.now().minusMinutes(1)).build();
        when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmailCode("ali@test.com", "123456"));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void verifyEmailCode_wrongPurposeCode_throwsBadRequest() {
        User user = User.builder().id(5L).email("ali@test.com").build();
        VerificationToken token = VerificationToken.builder()
                .token("123456").userId(5L).purpose(TokenPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).build();
        when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmailCode("ali@test.com", "123456"));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void forgotPassword_existingUser_sendsResetEmail() {
        User user = User.builder().id(6L).email("ali@test.com").build();
        when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));

        authService.forgotPassword("ali@test.com");

        verify(verificationTokenRepository).deleteByUserIdAndPurpose(6L, TokenPurpose.PASSWORD_RESET);
        verify(emailService).sendPasswordResetEmail(org.mockito.ArgumentMatchers.eq("ali@test.com"), any());
    }

    @Test
    void forgotPassword_unknownEmail_doesNothingSilently() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        authService.forgotPassword("missing@test.com");

        verifyNoInteractions(emailService);
    }

    @Test
    void resetPassword_validToken_updatesPassword() {
        VerificationToken token = VerificationToken.builder()
                .token("reset-token").userId(7L).purpose(TokenPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusHours(1)).build();
        User user = User.builder().id(7L).password("old-hash").build();
        when(verificationTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-hash");

        authService.resetPassword("reset-token", "newpassword123");

        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        VerificationToken token = VerificationToken.builder()
                .token("reset-token").userId(7L).purpose(TokenPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().minusMinutes(1)).build();
        when(verificationTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("reset-token", "newpassword123"));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(passwordEncoder);
    }
}
