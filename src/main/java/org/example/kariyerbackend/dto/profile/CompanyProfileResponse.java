package org.example.kariyerbackend.dto.profile;

public record CompanyProfileResponse(
        String companyName,
        String website,
        String logoUrl,
        String description
) {
}
