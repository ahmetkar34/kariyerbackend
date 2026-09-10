package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.admin.AdminStatsResponse;
import org.example.kariyerbackend.dto.admin.AdminUserResponse;
import org.example.kariyerbackend.dto.common.PageResponse;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.CandidateCertificateRepository;
import org.example.kariyerbackend.repository.CandidateEducationRepository;
import org.example.kariyerbackend.repository.CandidateProfileRepository;
import org.example.kariyerbackend.repository.EmployerProfileRepository;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.example.kariyerbackend.repository.VerificationTokenRepository;
import org.example.kariyerbackend.util.PageRequests;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateEducationRepository candidateEducationRepository;
    private final CandidateCertificateRepository candidateCertificateRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public PageResponse<AdminUserResponse> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequests.of(page, size, "createdAt");
        return PageResponse.from(userRepository.search(keyword, pageable).map(this::toResponse));
    }

    @Transactional
    public void deleteUser(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kendi hesabınızı silemezsiniz");
        }
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        jobPostingRepository.findByEmployerIdOrderByCreatedAtDesc(targetUserId).forEach(job -> {
            jobApplicationRepository.deleteByJobPostingId(job.getId());
            jobPostingRepository.delete(job);
        });
        jobApplicationRepository.deleteByCandidateId(targetUserId);
        candidateEducationRepository.deleteByCandidateId(targetUserId);
        candidateCertificateRepository.deleteByCandidateId(targetUserId);
        candidateProfileRepository.findById(targetUserId).ifPresent(candidateProfileRepository::delete);
        employerProfileRepository.findById(targetUserId).ifPresent(employerProfileRepository::delete);
        verificationTokenRepository.deleteByUserId(targetUserId);

        userRepository.delete(user);
    }

    public void deleteJob(Long jobId) {
        if (!jobPostingRepository.existsById(jobId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı");
        }
        jobPostingRepository.deleteById(jobId);
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.USER),
                userRepository.countByRole(Role.EMPLOYER),
                jobPostingRepository.count(),
                jobApplicationRepository.count()
        );
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
