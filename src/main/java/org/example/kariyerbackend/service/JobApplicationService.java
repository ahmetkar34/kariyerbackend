package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.ApplicationStatusResponse;
import org.example.kariyerbackend.dto.job.JobApplicationResponse;
import org.example.kariyerbackend.entity.JobApplication;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostingRepository jobPostingRepository;

    public ApplicationStatusResponse getStatus(Long jobId, Long candidateId) {
        boolean applied = jobApplicationRepository.existsByJobPostingIdAndCandidateId(jobId, candidateId);
        return new ApplicationStatusResponse(applied);
    }

    @Transactional
    public JobApplicationResponse apply(Long jobId, User candidate) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı"));

        if (jobApplicationRepository.existsByJobPostingIdAndCandidateId(jobId, candidate.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu ilana zaten başvurdunuz");
        }

        JobApplication application = JobApplication.builder()
                .jobPostingId(job.getId())
                .candidateId(candidate.getId())
                .candidateFirstName(candidate.getFirstName())
                .candidateLastName(candidate.getLastName())
                .candidateEmail(candidate.getEmail())
                .build();

        return toResponse(jobApplicationRepository.save(application));
    }

    public List<JobApplicationResponse> getApplicants(Long jobId, Long employerId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu ilan üzerinde yetkiniz yok");
        }

        return jobApplicationRepository.findByJobPostingIdOrderByCreatedAtDesc(jobId).stream()
                .map(this::toResponse)
                .toList();
    }

    private JobApplicationResponse toResponse(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getCandidateId(),
                application.getCandidateFirstName(),
                application.getCandidateLastName(),
                application.getCandidateEmail(),
                application.getCreatedAt()
        );
    }
}
