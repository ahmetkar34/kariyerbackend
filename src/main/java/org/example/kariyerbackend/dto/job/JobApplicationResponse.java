package org.example.kariyerbackend.dto.job;

import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        Long candidateId,
        String firstName,
        String lastName,
        String email,
        LocalDateTime appliedAt
) {
}
