package org.example.kariyerbackend.dto.job;

import org.example.kariyerbackend.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record MyApplicationResponse(
        Long id,
        Long jobPostingId,
        String jobTitle,
        String jobCompany,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
}
