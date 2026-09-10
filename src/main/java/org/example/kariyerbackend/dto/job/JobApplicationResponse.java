package org.example.kariyerbackend.dto.job;

import org.example.kariyerbackend.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        Long candidateId,
        String firstName,
        String lastName,
        String email,
        String coverLetter,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
}
