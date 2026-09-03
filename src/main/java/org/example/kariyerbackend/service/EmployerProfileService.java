package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
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
        return new EmployerProfileResponse(profile.getCompanyName());
    }

    public EmployerProfileResponse updateProfile(Long employerId, EmployerProfileRequest request) {
        EmployerProfile profile = findOrThrow(employerId);
        profile.setCompanyName(request.companyName());
        employerProfileRepository.save(profile);
        return new EmployerProfileResponse(profile.getCompanyName());
    }

    private EmployerProfile findOrThrow(Long employerId) {
        return employerProfileRepository.findById(employerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşveren profili bulunamadı"));
    }
}
