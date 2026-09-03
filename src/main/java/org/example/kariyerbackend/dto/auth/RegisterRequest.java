package org.example.kariyerbackend.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Ad boş olamaz")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        String lastName,

        @NotBlank(message = "E-posta boş olamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        String email,

        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
        String password,

        @AssertTrue(message = "Kullanım koşullarını kabul etmelisiniz")
        boolean termsAccepted,

        String role,

        String companyName
) {
}
