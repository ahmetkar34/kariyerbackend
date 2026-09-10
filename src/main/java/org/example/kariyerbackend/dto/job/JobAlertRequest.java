package org.example.kariyerbackend.dto.job;

public record JobAlertRequest(
        String keyword,
        String location,
        String type,
        Boolean remote
) {
}
