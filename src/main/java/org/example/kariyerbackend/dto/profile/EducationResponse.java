package org.example.kariyerbackend.dto.profile;

public record EducationResponse(
        Long id,
        String school,
        String degree,
        String startYear,
        String endYear
) {
}
