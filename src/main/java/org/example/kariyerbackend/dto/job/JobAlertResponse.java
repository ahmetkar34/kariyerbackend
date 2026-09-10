package org.example.kariyerbackend.dto.job;

import java.time.LocalDateTime;

public record JobAlertResponse(
        Long id,
        String keyword,
        String location,
        String type,
        Boolean remote,
        LocalDateTime createdAt
) {
}
