package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    public LoginResponse login(LoginRequest request) {
        CustomUserDetails userDetails;
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            userDetails = (CustomUserDetails) authentication.getPrincipal();
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
}
