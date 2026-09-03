package org.example.kariyerbackend.service;

import org.example.kariyerbackend.dto.admin.AdminStatsResponse;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void deleteUser_self_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adminService.deleteUser(1L, 1L));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        verify(userRepository, never()).findById(1L);
    }

    @Test
    void deleteUser_notFound_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adminService.deleteUser(1L, 2L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deleteUser_success_deletesUser() {
        User target = User.builder().id(2L).email("x@test.com").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.deleteUser(1L, 2L);

        verify(userRepository).delete(target);
    }

    @Test
    void deleteJob_notFound_throwsNotFound() {
        when(jobPostingRepository.existsById(9L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adminService.deleteJob(9L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        verify(jobPostingRepository, never()).deleteById(9L);
    }

    @Test
    void deleteJob_success_deletesJob() {
        when(jobPostingRepository.existsById(9L)).thenReturn(true);

        adminService.deleteJob(9L);

        verify(jobPostingRepository).deleteById(9L);
    }

    @Test
    void getStats_returnsAggregatedCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(Role.USER)).thenReturn(7L);
        when(userRepository.countByRole(Role.EMPLOYER)).thenReturn(3L);
        when(jobPostingRepository.count()).thenReturn(5L);
        when(jobApplicationRepository.count()).thenReturn(20L);

        AdminStatsResponse stats = adminService.getStats();

        assertThat(stats.totalUsers()).isEqualTo(10L);
        assertThat(stats.totalCandidates()).isEqualTo(7L);
        assertThat(stats.totalEmployers()).isEqualTo(3L);
        assertThat(stats.totalJobs()).isEqualTo(5L);
        assertThat(stats.totalApplications()).isEqualTo(20L);
    }
}
