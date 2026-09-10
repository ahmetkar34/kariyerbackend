package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.JobAlertRequest;
import org.example.kariyerbackend.dto.job.JobAlertResponse;
import org.example.kariyerbackend.entity.JobAlert;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobAlertService {

    private static final Logger log = LoggerFactory.getLogger(JobAlertService.class);

    private final JobAlertRepository jobAlertRepository;
    private final EmailService emailService;

    public JobAlertResponse create(User candidate, JobAlertRequest request) {
        boolean hasCriteria = isNotBlank(request.keyword())
                || isNotBlank(request.location())
                || isNotBlank(request.type())
                || Boolean.TRUE.equals(request.remote());
        if (!hasCriteria) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Uyarı için en az bir arama kriteri (anahtar kelime, konum, çalışma şekli veya uzaktan) gerekli");
        }

        JobAlert alert = JobAlert.builder()
                .candidateId(candidate.getId())
                .candidateEmail(candidate.getEmail())
                .candidateFirstName(candidate.getFirstName())
                .keyword(blankToNull(request.keyword()))
                .location(blankToNull(request.location()))
                .type(blankToNull(request.type()))
                .remote(request.remote())
                .build();

        return toResponse(jobAlertRepository.save(alert));
    }

    public List<JobAlertResponse> getMyAlerts(Long candidateId) {
        return jobAlertRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(Long id, Long candidateId) {
        JobAlert alert = jobAlertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uyarı bulunamadı"));
        if (!alert.getCandidateId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu uyarı üzerinde yetkiniz yok");
        }
        jobAlertRepository.delete(alert);
    }

    // Best-effort, fire-and-forget: called right after a new job posting is created, so a
    // failure here (bad SMTP, one bad address) must never affect the posting itself, which
    // has already been saved by the time this runs.
    public void notifyMatchingAlerts(JobPosting job) {
        for (JobAlert alert : jobAlertRepository.findAll()) {
            if (!matches(alert, job)) {
                continue;
            }
            try {
                emailService.sendJobAlertEmail(
                        alert.getCandidateEmail(), alert.getCandidateFirstName(),
                        job.getTitle(), job.getCompany(), job.getId());
            } catch (Exception ex) {
                log.warn("Failed to send job alert email to {}", alert.getCandidateEmail(), ex);
            }
        }
    }

    private boolean matches(JobAlert alert, JobPosting job) {
        if (isNotBlank(alert.getKeyword())) {
            String keyword = alert.getKeyword().toLowerCase();
            boolean keywordMatches = job.getTitle().toLowerCase().contains(keyword)
                    || job.getCompany().toLowerCase().contains(keyword)
                    || job.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(keyword));
            if (!keywordMatches) {
                return false;
            }
        }
        if (isNotBlank(alert.getLocation())
                && !job.getLocation().toLowerCase().contains(alert.getLocation().toLowerCase())) {
            return false;
        }
        if (isNotBlank(alert.getType()) && !alert.getType().equals(job.getType())) {
            return false;
        }
        return !Boolean.TRUE.equals(alert.getRemote()) || job.isRemote();
    }

    private JobAlertResponse toResponse(JobAlert alert) {
        return new JobAlertResponse(
                alert.getId(), alert.getKeyword(), alert.getLocation(), alert.getType(),
                alert.getRemote(), alert.getCreatedAt()
        );
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return isNotBlank(value) ? value.trim() : null;
    }
}
