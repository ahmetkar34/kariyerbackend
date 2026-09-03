package org.example.kariyerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.profile.ProfileRequest;
import org.example.kariyerbackend.dto.profile.ProfileResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.ProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ProfileResponse getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return profileService.getProfile(principal.getUser().getId());
    }

    @PutMapping("/me")
    public ProfileResponse updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ProfileRequest request
    ) {
        return profileService.saveProfile(principal.getUser().getId(), request);
    }
}
