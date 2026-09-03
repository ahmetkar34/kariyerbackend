package org.example.kariyerbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.profile.EmployerProfileRequest;
import org.example.kariyerbackend.dto.profile.EmployerProfileResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.EmployerProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employer/profile")
@RequiredArgsConstructor
public class EmployerProfileController {

    private final EmployerProfileService employerProfileService;

    @GetMapping
    public EmployerProfileResponse getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return employerProfileService.getProfile(principal.getUser().getId());
    }

    @PutMapping
    public EmployerProfileResponse updateProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody EmployerProfileRequest request
    ) {
        return employerProfileService.updateProfile(principal.getUser().getId(), request);
    }
}
