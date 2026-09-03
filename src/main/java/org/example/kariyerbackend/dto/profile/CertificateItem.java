package org.example.kariyerbackend.dto.profile;

public record CertificateItem(
        String name,
        String issuer,
        String year
) {
}
