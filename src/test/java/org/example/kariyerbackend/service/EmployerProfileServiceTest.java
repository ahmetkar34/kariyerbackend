package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.profile.EmployerProfileRequest;
import org.example.kariyerbackend.dto.profile.EmployerProfileResponse;
import org.example.kariyerbackend.entity.EmployerProfile;
import org.example.kariyerbackend.repository.EmployerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerProfileServiceTest {

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @InjectMocks
    private EmployerProfileService employerProfileService;

    @Test
    void getProfile_notFound_throwsNotFound() {
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> employerProfileService.getProfile(1L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getProfile_found_returnsCompanyName() {
        when(employerProfileRepository.findById(1L))
                .thenReturn(Optional.of(EmployerProfile.builder().userId(1L).companyName("Acme").build()));

        EmployerProfileResponse response = employerProfileService.getProfile(1L);

        assertThat(response.companyName()).isEqualTo("Acme");
    }

    @Test
    void updateProfile_updatesCompanyName() {
        EmployerProfile profile = EmployerProfile.builder().userId(1L).companyName("Old Name").build();
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        EmployerProfileResponse response = employerProfileService.updateProfile(1L, new EmployerProfileRequest("New Name", null, null, null));

        assertThat(response.companyName()).isEqualTo("New Name");
        assertThat(profile.getCompanyName()).isEqualTo("New Name");
    }

    @Test
    void updateProfile_setsWebsiteLogoAndDescription() {
        EmployerProfile profile = EmployerProfile.builder().userId(1L).companyName("Acme").build();
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        EmployerProfileResponse response = employerProfileService.updateProfile(1L, new EmployerProfileRequest(
                "Acme", "https://acme.example", "https://acme.example/logo.png", "  Acme yazılım şirketidir.  "));

        assertThat(response.website()).isEqualTo("https://acme.example");
        assertThat(response.logoUrl()).isEqualTo("https://acme.example/logo.png");
        assertThat(response.description()).isEqualTo("Acme yazılım şirketidir.");
    }

    @Test
    void updateProfile_blankOptionalFields_areStoredAsNull() {
        EmployerProfile profile = EmployerProfile.builder()
                .userId(1L).companyName("Acme").website("https://old.example").build();
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        EmployerProfileResponse response = employerProfileService.updateProfile(
                1L, new EmployerProfileRequest("Acme", "  ", "", null));

        assertThat(response.website()).isNull();
        assertThat(response.logoUrl()).isNull();
        assertThat(response.description()).isNull();
    }

    @Test
    void getPublicProfile_returnsCompanyDetails() {
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.of(EmployerProfile.builder()
                .userId(1L).companyName("Acme").website("https://acme.example").logoUrl("logo.png")
                .description("Hakkımızda").build()));

        var response = employerProfileService.getPublicProfile(1L);

        assertThat(response.companyName()).isEqualTo("Acme");
        assertThat(response.website()).isEqualTo("https://acme.example");
        assertThat(response.logoUrl()).isEqualTo("logo.png");
        assertThat(response.description()).isEqualTo("Hakkımızda");
    }

    @Test
    void getPublicProfile_notFound_throwsNotFound() {
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> employerProfileService.getPublicProfile(1L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void updateProfile_notFound_throwsNotFound() {
        when(employerProfileRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> employerProfileService.updateProfile(1L, new EmployerProfileRequest("New Name", null, null, null)));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }
}
