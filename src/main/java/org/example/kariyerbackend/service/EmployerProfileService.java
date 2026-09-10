package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.profile.CompanyProfileResponse;
import org.example.kariyerbackend.dto.profile.EmployerProfileRequest;
import org.example.kariyerbackend.dto.profile.EmployerProfileResponse;
import org.example.kariyerbackend.entity.EmployerProfile;
import org.example.kariyerbackend.repository.EmployerProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;

    public EmployerProfileResponse getProfile(Long employerId) {
        EmployerProfile profile = findOrThrow(employerId);
        return new EmployerProfileResponse(
                profile.getCompanyName(), profile.getWebsite(), profile.getLogoUrl(), profile.getDescription());
    }

    public EmployerProfileResponse updateProfile(Long employerId, EmployerProfileRequest request) {
        EmployerProfile profile = findOrThrow(employerId);
        profile.setCompanyName(request.companyName());
        profile.setWebsite(blankToNull(request.website()));
        profile.setLogoUrl(blankToNull(request.logoUrl()));
        profile.setDescription(blankToNull(request.description()));
        employerProfileRepository.save(profile);
        return new EmployerProfileResponse(
                profile.getCompanyName(), profile.getWebsite(), profile.getLogoUrl(), profile.getDescription());
    }

    // Public: shown on job detail pages to any visitor, so it must not leak anything
    // beyond what candidates should see about the company.
    public CompanyProfileResponse getPublicProfile(Long employerId) {
        EmployerProfile profile = findOrThrow(employerId);
        return new CompanyProfileResponse(
                profile.getCompanyName(), profile.getWebsite(), profile.getLogoUrl(), profile.getDescription());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private EmployerProfile findOrThrow(Long employerId) {
        return employerProfileRepository.findById(employerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşveren profili bulunamadı"));
    }
}
