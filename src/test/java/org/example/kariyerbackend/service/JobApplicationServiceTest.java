package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.job.ApplicationStatusResponse;
import org.example.kariyerbackend.dto.job.JobApplicationResponse;
import org.example.kariyerbackend.dto.job.MyApplicationResponse;
import org.example.kariyerbackend.entity.ApplicationStatus;
import org.example.kariyerbackend.entity.JobApplication;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User candidate() {
        return User.builder().id(10L).firstName("Ali").lastName("Veli").email("ali@test.com").build();
    }

    private JobPosting job() {
        return JobPosting.builder().id(5L).employerId(1L).title("Backend Developer").company("Acme").build();
    }

    @Test
    void apply_success_createsPendingApplication() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));
        when(jobApplicationRepository.existsByJobPostingIdAndCandidateId(5L, 10L)).thenReturn(false);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = jobApplicationService.apply(5L, candidate());

        assertThat(response.candidateId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void apply_jobNotFound_throwsNotFound() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.apply(5L, candidate()));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void apply_duplicateApplication_throwsConflict() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));
        when(jobApplicationRepository.existsByJobPostingIdAndCandidateId(5L, 10L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.apply(5L, candidate()));

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void updateStatus_byOwner_updatesStatus() {
        JobApplication application = JobApplication.builder().id(100L).jobPostingId(5L).candidateId(10L).build();
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = jobApplicationService.updateStatus(5L, 100L, 1L, ApplicationStatus.ACCEPTED);

        assertThat(response.status()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    @Test
    void updateStatus_byNonOwner_throwsForbidden() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.updateStatus(5L, 100L, 99L, ApplicationStatus.ACCEPTED));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        verify(jobApplicationRepository, never()).findById(any());
    }

    @Test
    void updateStatus_applicationNotFound_throwsNotFound() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.updateStatus(5L, 100L, 1L, ApplicationStatus.ACCEPTED));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void updateStatus_applicationBelongsToDifferentJob_throwsNotFound() {
        JobApplication application = JobApplication.builder().id(100L).jobPostingId(7L).candidateId(10L).build();
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.updateStatus(5L, 100L, 1L, ApplicationStatus.ACCEPTED));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void getApplicants_byNonOwner_throwsForbidden() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(job()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.getApplicants(5L, 99L));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void getMyApplications_mapsToResponse() {
        JobApplication application = JobApplication.builder()
                .id(100L).jobPostingId(5L).candidateId(10L)
                .jobTitle("Backend Developer").jobCompany("Acme")
                .build();
        when(jobApplicationRepository.findByCandidateIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(application));

        List<MyApplicationResponse> responses = jobApplicationService.getMyApplications(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).jobTitle()).isEqualTo("Backend Developer");
        assertThat(responses.get(0).status()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void getStatus_whenApplied_returnsAppliedTrue() {
        JobApplication application = JobApplication.builder()
                .id(100L).jobPostingId(5L).candidateId(10L).status(ApplicationStatus.ACCEPTED)
                .build();
        when(jobApplicationRepository.findByJobPostingIdAndCandidateId(5L, 10L)).thenReturn(Optional.of(application));

        ApplicationStatusResponse response = jobApplicationService.getStatus(5L, 10L);

        assertThat(response.applied()).isTrue();
        assertThat(response.status()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    @Test
    void getStatus_whenNotApplied_returnsAppliedFalse() {
        when(jobApplicationRepository.findByJobPostingIdAndCandidateId(5L, 10L)).thenReturn(Optional.empty());

        ApplicationStatusResponse response = jobApplicationService.getStatus(5L, 10L);

        assertThat(response.applied()).isFalse();
        assertThat(response.status()).isNull();
    }
}
