package org.example.kariyerbackend.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record EmployerProfileRequest(
        @NotBlank(message = "Şirket adı gerekli")
        String companyName
) {
}
