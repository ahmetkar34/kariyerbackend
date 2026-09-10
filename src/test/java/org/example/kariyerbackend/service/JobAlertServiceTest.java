package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.job.JobAlertRequest;
import org.example.kariyerbackend.dto.job.JobAlertResponse;
import org.example.kariyerbackend.entity.JobAlert;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobAlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobAlertServiceTest {

    @Mock
    private JobAlertRepository jobAlertRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private JobAlertService jobAlertService;

    private User candidate() {
        return User.builder().id(10L).firstName("Ali").email("ali@test.com").build();
    }

    private JobPosting job() {
        return JobPosting.builder()
                .id(1L).title("Backend Developer").company("Acme").location("Istanbul")
                .type("Tam Zamanlı").remote(false).tags(List.of("Java", "Spring"))
                .build();
    }

    @Test
    void create_withNoCriteria_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobAlertService.create(candidate(), new JobAlertRequest(null, "  ", null, null)));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        verify(jobAlertRepository, never()).save(any());
    }

    @Test
    void create_withKeyword_savesAlertWithCandidateSnapshot() {
        when(jobAlertRepository.save(any(JobAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        JobAlertResponse response = jobAlertService.create(candidate(), new JobAlertRequest("java", null, null, null));

        assertThat(response.keyword()).isEqualTo("java");
    }

    @Test
    void delete_byOwner_deletesAlert() {
        JobAlert alert = JobAlert.builder().id(1L).candidateId(10L).build();
        when(jobAlertRepository.findById(1L)).thenReturn(Optional.of(alert));

        jobAlertService.delete(1L, 10L);

        verify(jobAlertRepository).delete(alert);
    }

    @Test
    void delete_byNonOwner_throwsForbidden() {
        JobAlert alert = JobAlert.builder().id(1L).candidateId(10L).build();
        when(jobAlertRepository.findById(1L)).thenReturn(Optional.of(alert));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobAlertService.delete(1L, 99L));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        verify(jobAlertRepository, never()).delete(any());
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(jobAlertRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobAlertService.delete(1L, 10L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void notifyMatchingAlerts_keywordMatchesTitle_sendsEmail() {
        JobAlert alert = JobAlert.builder()
                .id(1L).candidateId(10L).candidateEmail("ali@test.com").candidateFirstName("Ali")
                .keyword("backend").build();
        when(jobAlertRepository.findAll()).thenReturn(List.of(alert));

        jobAlertService.notifyMatchingAlerts(job());

        verify(emailService).sendJobAlertEmail("ali@test.com", "Ali", "Backend Developer", "Acme", 1L);
    }

    @Test
    void notifyMatchingAlerts_keywordMatchesTag_sendsEmail() {
        JobAlert alert = JobAlert.builder()
                .id(1L).candidateId(10L).candidateEmail("ali@test.com").candidateFirstName("Ali")
                .keyword("spring").build();
        when(jobAlertRepository.findAll()).thenReturn(List.of(alert));

        jobAlertService.notifyMatchingAlerts(job());

        verify(emailService).sendJobAlertEmail(any(), any(), any(), any(), any());
    }

    @Test
    void notifyMatchingAlerts_keywordDoesNotMatch_doesNotSendEmail() {
        JobAlert alert = JobAlert.builder()
                .id(1L).candidateId(10L).candidateEmail("ali@test.com").candidateFirstName("Ali")
                .keyword("frontend").build();
        when(jobAlertRepository.findAll()).thenReturn(List.of(alert));

        jobAlertService.notifyMatchingAlerts(job());

        verify(emailService, never()).sendJobAlertEmail(any(), any(), any(), any(), any());
    }

    @Test
    void notifyMatchingAlerts_locationDoesNotMatch_doesNotSendEmail() {
        JobAlert alert = JobAlert.builder()
                .id(1L).candidateId(10L).candidateEmail("ali@test.com").candidateFirstName("Ali")
                .location("Ankara").build();
        when(jobAlertRepository.findAll()).thenReturn(List.of(alert));

        jobAlertService.notifyMatchingAlerts(job());

        verify(emailService, never()).sendJobAlertEmail(any(), any(), any(), any(), any());
    }

    @Test
    void notifyMatchingAlerts_remoteOnlyAlert_skipsNonRemoteJob() {
        JobAlert alert = JobAlert.builder()
                .id(1L).candidateId(10L).candidateEmail("ali@test.com").candidateFirstName("Ali")
                .remote(true).build();
        when(jobAlertRepository.findAll()).thenReturn(List.of(alert));

        jobAlertService.notifyMatchingAlerts(job());

        verify(emailService, never()).sendJobAlertEmail(any(), any(), any(), any(), any());
    }

    @Test
    void notifyMatchingAlerts_whenEmailFails_doesNotPropagate() {
        JobAlert alert = JobAlert.builder()
                .id(1L).candidateId(10L).candidateEmail("ali@test.com").candidateFirstName("Ali")
                .keyword("backend").build();
        when(jobAlertRepository.findAll()).thenReturn(List.of(alert));
        doThrow(new IllegalStateException("SMTP down")).when(emailService)
                .sendJobAlertEmail(any(), any(), any(), any(), any());

        assertDoesNotThrow(() -> jobAlertService.notifyMatchingAlerts(job()));
    }
}
