package org.example.kariyerbackend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Token gerekli")
        String token
) {
}
