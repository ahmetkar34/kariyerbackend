package org.example.kariyerbackend.dto.profile;

import java.util.List;

public record ProfileRequest(
        String phone,
        String title,
        String summary,
        List<EducationItem> education,
        List<CertificateItem> certificates
) {
}
