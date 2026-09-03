package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu e-posta adresi zaten kayıtlı");
        }

        boolean isEmployer = "employer".equalsIgnoreCase(request.role());
        if (isEmployer && (request.companyName() == null || request.companyName().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Şirket adı gerekli");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .termsAccepted(request.termsAccepted())
                .role(isEmployer ? Role.EMPLOYER : Role.USER)
                .build();

        User saved = userRepository.save(user);

        String companyName = null;
        if (isEmployer) {
            EmployerProfile profile = EmployerProfile.builder()
                    .user(saved)
                    .companyName(request.companyName())
                    .build();
            companyName = employerProfileRepository.save(profile).getCompanyName();
        }

        sendVerificationEmail(saved);

        return new RegisterResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRole(),
                companyName,
                saved.getCreatedAt()
        );
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz doğrulama bağlantısı"));

        assertUsable(verificationToken, TokenPurpose.EMAIL_VERIFICATION);

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                sendVerificationEmail(user);
            }
        });
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(), TokenPurpose.PASSWORD_RESET);
            String token = UUID.randomUUID().toString();
            verificationTokenRepository.save(VerificationToken.builder()
                    .token(token)
                    .userId(user.getId())
                    .purpose(TokenPurpose.PASSWORD_RESET)
                    .expiresAt(LocalDateTime.now().plusHours(PASSWORD_RESET_EXPIRY_HOURS))
                    .build());
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz veya süresi dolmuş bağlantı"));

        assertUsable(verificationToken, TokenPurpose.PASSWORD_RESET);

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);
    }

    public LoginResponse login(LoginRequest request) {
        CustomUserDetails userDetails;
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            userDetails = (CustomUserDetails) authentication.getPrincipal();
        } catch (DisabledException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "E-posta adresinizi doğrulamanız gerekiyor");
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-posta veya şifre hatalı");
        }

        User user = userDetails.getUser();
        String token = jwtService.generateToken(userDetails);

        String companyName = user.getRole() == Role.EMPLOYER
                ? employerProfileRepository.findById(user.getId()).map(EmployerProfile::getCompanyName).orElse(null)
                : null;

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                companyName
        );
    }

    private void sendVerificationEmail(User user) {
        verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(), TokenPurpose.EMAIL_VERIFICATION);
        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(VerificationToken.builder()
                .token(token)
                .userId(user.getId())
                .purpose(TokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_EXPIRY_HOURS))
                .build());
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    private void assertUsable(VerificationToken token, TokenPurpose expectedPurpose) {
        if (token.getPurpose() != expectedPurpose
                || token.getUsedAt() != null
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz veya süresi dolmuş bağlantı");
        }
    }
}
