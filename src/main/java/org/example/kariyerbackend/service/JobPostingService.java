package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.common.PageResponse;
import org.example.kariyerbackend.dto.job.JobPostingRequest;
import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.SavedJobRepository;
import org.example.kariyerbackend.util.PageRequests;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SavedJobRepository savedJobRepository;

    public PageResponse<JobPostingResponse> search(String keyword, String location, int page, int size) {
        Pageable pageable = PageRequests.of(page, size, "createdAt");
        return PageResponse.from(jobPostingRepository.search(keyword, location, pageable).map(this::toResponse));
    }

    @Transactional
    public JobPostingResponse getById(Long id, Long viewerId) {
        JobPosting job = findOrThrow(id);
        // Don't let an employer's own visits to their listing inflate its view count.
        if (viewerId == null || !viewerId.equals(job.getEmployerId())) {
            jobPostingRepository.incrementViewCount(id);
            job.setViewCount(job.getViewCount() + 1);
        }
        return toResponse(job);
    }

    public List<JobPostingResponse> getByEmployer(Long employerId) {
        return jobPostingRepository.findByEmployerIdOrderByCreatedAtDesc(employerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public JobPostingResponse create(Long employerId, JobPostingRequest request) {
        JobPosting job = JobPosting.builder()
                .employerId(employerId)
                .title(request.title())
                .company(request.company())
                .location(request.location())
                .type(request.type())
                .remote(request.remote())
                .salary(request.salary())
                .tags(request.tags())
                .description(request.description())
                .responsibilities(request.responsibilities())
                .requirements(request.requirements())
                .aboutCompany(request.aboutCompany())
                .build();

        return toResponse(jobPostingRepository.save(job));
    }

    public JobPostingResponse update(Long id, Long employerId, JobPostingRequest request) {
        JobPosting job = findOrThrow(id);
        assertOwner(job, employerId);

        job.setTitle(request.title());
        job.setCompany(request.company());
        job.setLocation(request.location());
        job.setType(request.type());
        job.setRemote(request.remote());
        job.setSalary(request.salary());
        job.setTags(request.tags());
        job.setDescription(request.description());
        job.setResponsibilities(request.responsibilities());
        job.setRequirements(request.requirements());
        job.setAboutCompany(request.aboutCompany());

        return toResponse(jobPostingRepository.save(job));
    }

    @Transactional
    public void delete(Long id, Long employerId) {
        JobPosting job = findOrThrow(id);
        assertOwner(job, employerId);
        jobApplicationRepository.deleteByJobPostingId(id);
        savedJobRepository.deleteByJobPostingId(id);
        jobPostingRepository.delete(job);
    }

    private JobPosting findOrThrow(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı"));
    }

    private void assertOwner(JobPosting job, Long employerId) {
        if (!job.getEmployerId().equals(employerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu ilan üzerinde yetkiniz yok");
        }
    }

    JobPostingResponse toResponse(JobPosting job) {
        return new JobPostingResponse(
                job.getId(),
                job.getEmployerId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getType(),
                job.isRemote(),
                job.getSalary(),
                job.getTags(),
                job.getDescription(),
                job.getResponsibilities(),
                job.getRequirements(),
                job.getAboutCompany(),
                job.getViewCount(),
                job.getCreatedAt()
        );
    }
}
