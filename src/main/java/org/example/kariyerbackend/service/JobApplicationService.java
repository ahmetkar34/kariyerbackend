package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.ApplicationStatusResponse;
import org.example.kariyerbackend.dto.job.JobApplicationResponse;
import org.example.kariyerbackend.dto.job.MyApplicationResponse;
import org.example.kariyerbackend.entity.ApplicationStatus;
import org.example.kariyerbackend.entity.JobApplication;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationService.class);

    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final EmailService emailService;

    public ApplicationStatusResponse getStatus(Long jobId, Long candidateId) {
        return jobApplicationRepository.findByJobPostingIdAndCandidateId(jobId, candidateId)
                .map(app -> new ApplicationStatusResponse(true, app.getStatus()))
                .orElseGet(() -> new ApplicationStatusResponse(false, null));
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
                .jobTitle(job.getTitle())
                .jobCompany(job.getCompany())
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

    public List<MyApplicationResponse> getMyApplications(Long candidateId) {
        return jobApplicationRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId).stream()
                .map(app -> new MyApplicationResponse(
                        app.getId(),
                        app.getJobPostingId(),
                        app.getJobTitle(),
                        app.getJobCompany(),
                        app.getStatus(),
                        app.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public JobApplicationResponse updateStatus(Long jobId, Long applicationId, Long employerId, ApplicationStatus status) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu ilan üzerinde yetkiniz yok");
        }

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Başvuru bulunamadı"));

        if (!application.getJobPostingId().equals(jobId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Başvuru bulunamadı");
        }

        application.setStatus(status);
        JobApplication saved = jobApplicationRepository.save(application);
        notifyCandidateOfStatusChange(saved);
        return toResponse(saved);
    }

    private void notifyCandidateOfStatusChange(JobApplication application) {
        if (application.getStatus() != ApplicationStatus.ACCEPTED && application.getStatus() != ApplicationStatus.REJECTED) {
            return;
        }
        // Best-effort: an SMTP outage must not roll back the status change itself
        // (this method runs inside updateStatus's @Transactional), just the notification.
        try {
            emailService.sendApplicationStatusEmail(
                    application.getCandidateEmail(),
                    application.getCandidateFirstName(),
                    application.getJobTitle(),
                    application.getJobCompany(),
                    application.getStatus()
            );
        } catch (Exception ex) {
            log.warn("Failed to send application status email to {}", application.getCandidateEmail(), ex);
        }
    }

    private JobApplicationResponse toResponse(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getCandidateId(),
                application.getCandidateFirstName(),
                application.getCandidateLastName(),
                application.getCandidateEmail(),
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}
