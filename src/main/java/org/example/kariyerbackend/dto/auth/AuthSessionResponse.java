package org.example.kariyerbackend.dto.auth;

public record AuthSessionResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String companyName
) {
    public static AuthSessionResponse from(LoginResponse login) {
        return new AuthSessionResponse(
                login.id(),
                login.firstName(),
                login.lastName(),
                login.email(),
                login.role(),
                login.companyName()
        );
    }
}
