package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.profile.CertificateItem;
import org.example.kariyerbackend.dto.profile.EducationItem;
import org.example.kariyerbackend.dto.profile.ProfileRequest;
import org.example.kariyerbackend.dto.profile.ProfileResponse;
import org.example.kariyerbackend.entity.CandidateCertificate;
import org.example.kariyerbackend.entity.CandidateEducation;
import org.example.kariyerbackend.entity.CandidateProfile;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.CandidateCertificateRepository;
import org.example.kariyerbackend.repository.CandidateEducationRepository;
import org.example.kariyerbackend.repository.CandidateProfileRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private CandidateEducationRepository candidateEducationRepository;
    @Mock
    private CandidateCertificateRepository candidateCertificateRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void saveProfile_newProfile_usesUserReferenceForCreation() {
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(1L)).thenReturn(User.builder().id(1L).build());
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(candidateEducationRepository.findByCandidateId(1L)).thenReturn(List.of());
        when(candidateCertificateRepository.findByCandidateId(1L)).thenReturn(List.of());

        ProfileRequest request = new ProfileRequest("555", "Developer", "Summary", List.of(), List.of());

        ProfileResponse response = profileService.saveProfile(1L, request);

        assertThat(response.phone()).isEqualTo("555");
        verify(userRepository).getReferenceById(1L);
    }

    @Test
    void saveProfile_existingProfile_doesNotFetchUserReference() {
        when(candidateProfileRepository.findById(1L))
                .thenReturn(Optional.of(CandidateProfile.builder().userId(1L).build()));
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(candidateEducationRepository.findByCandidateId(1L)).thenReturn(List.of());
        when(candidateCertificateRepository.findByCandidateId(1L)).thenReturn(List.of());

        ProfileRequest request = new ProfileRequest("555", "Developer", "Summary", List.of(), List.of());

        profileService.saveProfile(1L, request);

        verify(userRepository, org.mockito.Mockito.never()).getReferenceById(any());
    }

    @Test
    void saveProfile_filtersOutFullyBlankEducationEntries() {
        when(candidateProfileRepository.findById(1L))
                .thenReturn(Optional.of(CandidateProfile.builder().userId(1L).build()));
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(candidateEducationRepository.findByCandidateId(1L)).thenReturn(List.of());
        when(candidateCertificateRepository.findByCandidateId(1L)).thenReturn(List.of());

        List<EducationItem> education = List.of(
                new EducationItem("", "", "", ""),
                new EducationItem("MIT", "CS", "2020", "2024")
        );
        ProfileRequest request = new ProfileRequest(null, null, null, education, List.of());

        profileService.saveProfile(1L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CandidateEducation>> captor = ArgumentCaptor.forClass(List.class);
        verify(candidateEducationRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getSchool()).isEqualTo("MIT");
    }

    @Test
    void saveProfile_filtersOutFullyBlankCertificateEntries() {
        when(candidateProfileRepository.findById(1L))
                .thenReturn(Optional.of(CandidateProfile.builder().userId(1L).build()));
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(candidateEducationRepository.findByCandidateId(1L)).thenReturn(List.of());
        when(candidateCertificateRepository.findByCandidateId(1L)).thenReturn(List.of());

        List<CertificateItem> certificates = List.of(
                new CertificateItem("", "", ""),
                new CertificateItem("AWS Developer", "AWS", "2023")
        );
        ProfileRequest request = new ProfileRequest(null, null, null, List.of(), certificates);

        profileService.saveProfile(1L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CandidateCertificate>> captor = ArgumentCaptor.forClass(List.class);
        verify(candidateCertificateRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getName()).isEqualTo("AWS Developer");
    }

    @Test
    void getProfile_whenNoProfileRow_returnsNullFieldsWithEmptyLists() {
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(candidateEducationRepository.findByCandidateId(1L)).thenReturn(List.of());
        when(candidateCertificateRepository.findByCandidateId(1L)).thenReturn(List.of());

        ProfileResponse response = profileService.getProfile(1L);

        assertThat(response.phone()).isNull();
        assertThat(response.education()).isEmpty();
        assertThat(response.certificates()).isEmpty();
    }
}
