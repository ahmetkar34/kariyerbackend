package org.example.kariyerbackend.dto.profile;

import java.util.List;

public record ProfileResponse(
        String phone,
        String title,
        String summary,
        List<EducationResponse> education,
        List<CertificateResponse> certificates
) {
}
