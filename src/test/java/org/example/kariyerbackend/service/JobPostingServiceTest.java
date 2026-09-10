package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.job.JobPostingRequest;
import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.SavedJobRepository;
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
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private SavedJobRepository savedJobRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    private JobPostingRequest sampleRequest() {
        return new JobPostingRequest(
                "Backend Developer", "Acme", "Istanbul", "Tam Zamanlı", true, "50000",
                List.of("Java", "Spring"), "description", List.of(), List.of(), "about"
        );
    }

    @Test
    void create_setsEmployerIdFromCaller() {
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPostingResponse response = jobPostingService.create(42L, sampleRequest());

        assertThat(response.employerId()).isEqualTo(42L);
        assertThat(response.title()).isEqualTo("Backend Developer");
    }

    @Test
    void update_byOwner_updatesFields() {
        JobPosting existing = JobPosting.builder().id(1L).employerId(42L).title("Old Title").build();
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPostingResponse response = jobPostingService.update(1L, 42L, sampleRequest());

        assertThat(response.title()).isEqualTo("Backend Developer");
    }

    @Test
    void update_byNonOwner_throwsForbidden() {
        JobPosting existing = JobPosting.builder().id(1L).employerId(42L).title("Old Title").build();
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobPostingService.update(1L, 99L, sampleRequest()));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void update_jobNotFound_throwsNotFound() {
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobPostingService.update(1L, 42L, sampleRequest()));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void delete_byOwner_deletesJob() {
        JobPosting existing = JobPosting.builder().id(1L).employerId(42L).build();
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existing));

        jobPostingService.delete(1L, 42L);

        verify(jobApplicationRepository).deleteByJobPostingId(1L);
        verify(savedJobRepository).deleteByJobPostingId(1L);
        verify(jobPostingRepository).delete(existing);
    }

    @Test
    void delete_byNonOwner_throwsForbidden() {
        JobPosting existing = JobPosting.builder().id(1L).employerId(42L).build();
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobPostingService.delete(1L, 99L));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        verify(jobPostingRepository, never()).delete(any());
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobPostingService.getById(5L, null));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getById_found_returnsResponse() {
        JobPosting existing = JobPosting.builder().id(5L).employerId(1L).title("Title").viewCount(9L).build();
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(existing));

        JobPostingResponse response = jobPostingService.getById(5L, null);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.title()).isEqualTo("Title");
    }

    @Test
    void getById_byAnonymousOrOtherUser_incrementsViewCount() {
        JobPosting existing = JobPosting.builder().id(5L).employerId(1L).title("Title").viewCount(9L).build();
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(existing));

        JobPostingResponse anonymousView = jobPostingService.getById(5L, null);
        assertThat(anonymousView.viewCount()).isEqualTo(10L);
        verify(jobPostingRepository).incrementViewCount(5L);

        JobPostingResponse otherUserView = jobPostingService.getById(5L, 99L);
        assertThat(otherUserView.viewCount()).isEqualTo(11L);
    }

    @Test
    void getById_byOwner_doesNotIncrementViewCount() {
        JobPosting existing = JobPosting.builder().id(5L).employerId(1L).title("Title").viewCount(9L).build();
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(existing));

        JobPostingResponse response = jobPostingService.getById(5L, 1L);

        assertThat(response.viewCount()).isEqualTo(9L);
        verify(jobPostingRepository, never()).incrementViewCount(any());
    }
}
