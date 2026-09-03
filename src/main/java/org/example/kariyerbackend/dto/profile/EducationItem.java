package org.example.kariyerbackend.dto.profile;

public record EducationItem(
        String school,
        String degree,
        String startYear,
        String endYear
) {
}
