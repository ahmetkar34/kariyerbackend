package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.profile.CertificateItem;
import org.example.kariyerbackend.dto.profile.CertificateResponse;
import org.example.kariyerbackend.dto.profile.EducationItem;
import org.example.kariyerbackend.dto.profile.EducationResponse;
import org.example.kariyerbackend.dto.profile.ProfileRequest;
import org.example.kariyerbackend.dto.profile.ProfileResponse;
import org.example.kariyerbackend.entity.CandidateCertificate;
import org.example.kariyerbackend.entity.CandidateEducation;
import org.example.kariyerbackend.entity.CandidateProfile;
import org.example.kariyerbackend.repository.CandidateCertificateRepository;
import org.example.kariyerbackend.repository.CandidateEducationRepository;
import org.example.kariyerbackend.repository.CandidateProfileRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateEducationRepository candidateEducationRepository;
    private final CandidateCertificateRepository candidateCertificateRepository;
    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long candidateId) {
        CandidateProfile profile = candidateProfileRepository.findById(candidateId).orElse(null);
        List<CandidateEducation> educations = candidateEducationRepository.findByCandidateId(candidateId);
        List<CandidateCertificate> certificates = candidateCertificateRepository.findByCandidateId(candidateId);
        return toResponse(profile, educations, certificates);
    }

    @Transactional
    public ProfileResponse saveProfile(Long candidateId, ProfileRequest request) {
        CandidateProfile profile = candidateProfileRepository.findById(candidateId)
                .orElseGet(() -> CandidateProfile.builder()
                        .user(userRepository.getReferenceById(candidateId))
                        .build());
        profile.setPhone(request.phone());
        profile.setTitle(request.title());
        profile.setSummary(request.summary());
        candidateProfileRepository.save(profile);

        candidateEducationRepository.deleteByCandidateId(candidateId);
        List<CandidateEducation> educations = request.education() == null
                ? List.of()
                : request.education().stream()
                        .filter(ProfileService::hasContent)
                        .map(e -> CandidateEducation.builder()
                                .candidateId(candidateId)
                                .school(e.school())
                                .degree(e.degree())
                                .startYear(e.startYear())
                                .endYear(e.endYear())
                                .build())
                        .toList();
        candidateEducationRepository.saveAll(educations);

        candidateCertificateRepository.deleteByCandidateId(candidateId);
        List<CandidateCertificate> certificates = request.certificates() == null
                ? List.of()
                : request.certificates().stream()
                        .filter(ProfileService::hasContent)
                        .map(c -> CandidateCertificate.builder()
                                .candidateId(candidateId)
                                .name(c.name())
                                .issuer(c.issuer())
                                .year(c.year())
                                .build())
                        .toList();
        candidateCertificateRepository.saveAll(certificates);

        return toResponse(
                profile,
                candidateEducationRepository.findByCandidateId(candidateId),
                candidateCertificateRepository.findByCandidateId(candidateId)
        );
    }

    private static boolean hasContent(EducationItem item) {
        return isNotBlank(item.school()) || isNotBlank(item.degree())
                || isNotBlank(item.startYear()) || isNotBlank(item.endYear());
    }

    private static boolean hasContent(CertificateItem item) {
        return isNotBlank(item.name()) || isNotBlank(item.issuer()) || isNotBlank(item.year());
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private ProfileResponse toResponse(
            CandidateProfile profile,
            List<CandidateEducation> educations,
            List<CandidateCertificate> certificates
    ) {
        return new ProfileResponse(
                profile != null ? profile.getPhone() : null,
                profile != null ? profile.getTitle() : null,
                profile != null ? profile.getSummary() : null,
                educations.stream()
                        .map(e -> new EducationResponse(e.getId(), e.getSchool(), e.getDegree(), e.getStartYear(), e.getEndYear()))
                        .toList(),
                certificates.stream()
                        .map(c -> new CertificateResponse(c.getId(), c.getName(), c.getIssuer(), c.getYear()))
                        .toList()
        );
    }
}
