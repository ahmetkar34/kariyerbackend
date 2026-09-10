package org.example.kariyerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.profile.CompanyProfileResponse;
import org.example.kariyerbackend.service.EmployerProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final EmployerProfileService employerProfileService;

    @GetMapping("/{employerId}")
    public CompanyProfileResponse getPublicProfile(@PathVariable Long employerId) {
        return employerProfileService.getPublicProfile(employerId);
    }
}
