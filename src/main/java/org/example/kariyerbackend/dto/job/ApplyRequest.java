package org.example.kariyerbackend.dto.job;

import jakarta.validation.constraints.Size;

public record ApplyRequest(
        @Size(max = 4000, message = "Ön yazı en fazla 4000 karakter olabilir")
        String coverLetter
) {
}
