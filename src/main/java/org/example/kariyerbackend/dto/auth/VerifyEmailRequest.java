package org.example.kariyerbackend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "E-posta boş olamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        String email,

        @NotBlank(message = "Kod gerekli")
        String code
) {
}
