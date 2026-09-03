package org.example.kariyerbackend.dto.admin;

import org.example.kariyerbackend.entity.Role;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}
