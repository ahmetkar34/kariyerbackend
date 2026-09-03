package org.example.kariyerbackend.dto.job;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record JobPostingRequest(

        @NotBlank(message = "İlan başlığı gerekli")
        String title,

        @NotBlank(message = "Şirket adı gerekli")
        String company,

        @NotBlank(message = "Konum gerekli")
        String location,

        String type,

        boolean remote,

        String salary,

        List<String> tags,

        @NotBlank(message = "İlan açıklaması gerekli")
        String description,

        List<String> responsibilities,

        List<String> requirements,

        String aboutCompany
) {
}
