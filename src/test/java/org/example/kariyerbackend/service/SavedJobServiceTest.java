package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.dto.job.SavedStatusResponse;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.SavedJob;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.SavedJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private JobPostingService jobPostingService;

    @InjectMocks
    private SavedJobService savedJobService;

    @Test
    void getStatus_whenSaved_returnsTrue() {
        when(savedJobRepository.existsByCandidateIdAndJobPostingId(10L, 5L)).thenReturn(true);

        SavedStatusResponse response = savedJobService.getStatus(5L, 10L);

        assertThat(response.saved()).isTrue();
    }

    @Test
    void getStatus_whenNotSaved_returnsFalse() {
        when(savedJobRepository.existsByCandidateIdAndJobPostingId(10L, 5L)).thenReturn(false);

        SavedStatusResponse response = savedJobService.getStatus(5L, 10L);

        assertThat(response.saved()).isFalse();
    }

    @Test
    void save_notYetSaved_createsSavedJob() {
        when(savedJobRepository.existsByCandidateIdAndJobPostingId(10L, 5L)).thenReturn(false);
        when(jobPostingRepository.existsById(5L)).thenReturn(true);

        savedJobService.save(5L, 10L);

        verify(savedJobRepository).save(any(SavedJob.class));
    }

    @Test
    void save_alreadySaved_isIdempotent() {
        when(savedJobRepository.existsByCandidateIdAndJobPostingId(10L, 5L)).thenReturn(true);

        savedJobService.save(5L, 10L);

        verify(savedJobRepository, never()).save(any());
        verify(jobPostingRepository, never()).existsById(any());
    }

    @Test
    void save_jobNotFound_throwsNotFound() {
        when(savedJobRepository.existsByCandidateIdAndJobPostingId(10L, 5L)).thenReturn(false);
        when(jobPostingRepository.existsById(5L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> savedJobService.save(5L, 10L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        verify(savedJobRepository, never()).save(any());
    }

    @Test
    void unsave_deletesTheSavedJobRow() {
        savedJobService.unsave(5L, 10L);

        verify(savedJobRepository).deleteByCandidateIdAndJobPostingId(10L, 5L);
    }

    @Test
    void getMyFavorites_returnsJobsInMostRecentlySavedOrder() {
        SavedJob savedNewer = SavedJob.builder().id(1L).candidateId(10L).jobPostingId(6L).build();
        SavedJob savedOlder = SavedJob.builder().id(2L).candidateId(10L).jobPostingId(5L).build();
        JobPosting job5 = JobPosting.builder().id(5L).title("Backend Developer").build();
        JobPosting job6 = JobPosting.builder().id(6L).title("Frontend Developer").build();
        JobPostingResponse response5 = jobResponse(5L, "Backend Developer");
        JobPostingResponse response6 = jobResponse(6L, "Frontend Developer");

        when(savedJobRepository.findByCandidateIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(savedNewer, savedOlder));
        when(jobPostingRepository.findAllById(List.of(6L, 5L))).thenReturn(List.of(job5, job6));
        when(jobPostingService.toResponse(job5)).thenReturn(response5);
        when(jobPostingService.toResponse(job6)).thenReturn(response6);

        List<JobPostingResponse> favorites = savedJobService.getMyFavorites(10L);

        assertThat(favorites).containsExactly(response6, response5);
    }

    @Test
    void getMyFavorites_skipsJobsThatNoLongerExist() {
        SavedJob saved = SavedJob.builder().id(1L).candidateId(10L).jobPostingId(5L).build();

        when(savedJobRepository.findByCandidateIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(saved));
        when(jobPostingRepository.findAllById(List.of(5L))).thenReturn(List.of());

        List<JobPostingResponse> favorites = savedJobService.getMyFavorites(10L);

        assertThat(favorites).isEmpty();
    }

    private JobPostingResponse jobResponse(Long id, String title) {
        return new JobPostingResponse(
                id, 1L, title, "Acme", "Istanbul", "Tam Zamanlı", false, "50000",
                List.of(), "description", List.of(), List.of(), null, 0L, LocalDateTime.now()
        );
    }
}
