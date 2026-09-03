package org.example.kariyerbackend.dto.auth;

import org.example.kariyerbackend.entity.Role;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        String companyName,
        LocalDateTime createdAt
) {
}
