package org.example.kariyerbackend.dto.job;

import org.example.kariyerbackend.entity.ApplicationStatus;

public record UpdateApplicationStatusRequest(
        ApplicationStatus status
) {
}
