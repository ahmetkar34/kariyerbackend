package org.example.kariyerbackend.dto.profile;

public record CertificateResponse(
        Long id,
        String name,
        String issuer,
        String year
) {
}
