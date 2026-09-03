package org.example.kariyerbackend.dto.auth;

public record LoginResponse(
        String token,
        String tokenType,
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String companyName
) {
}
