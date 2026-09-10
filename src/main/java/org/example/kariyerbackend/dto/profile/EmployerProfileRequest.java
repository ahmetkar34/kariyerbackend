package org.example.kariyerbackend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployerProfileRequest(
        @NotBlank(message = "Şirket adı gerekli")
        String companyName,

        @Size(max = 300, message = "Website adresi en fazla 300 karakter olabilir")
        String website,

        @Size(max = 500, message = "Logo adresi en fazla 500 karakter olabilir")
        String logoUrl,

        @Size(max = 2000, message = "Açıklama en fazla 2000 karakter olabilir")
        String description
) {
}
