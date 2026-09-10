package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.dto.job.SavedStatusResponse;
import org.example.kariyerbackend.entity.JobPosting;
import org.example.kariyerbackend.entity.SavedJob;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.SavedJobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingService jobPostingService;

    public SavedStatusResponse getStatus(Long jobId, Long candidateId) {
        return new SavedStatusResponse(savedJobRepository.existsByCandidateIdAndJobPostingId(candidateId, jobId));
    }

    @Transactional
    public void save(Long jobId, Long candidateId) {
        if (savedJobRepository.existsByCandidateIdAndJobPostingId(candidateId, jobId)) {
            return;
        }
        if (!jobPostingRepository.existsById(jobId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı");
        }
        savedJobRepository.save(SavedJob.builder().candidateId(candidateId).jobPostingId(jobId).build());
    }

    public void unsave(Long jobId, Long candidateId) {
        savedJobRepository.deleteByCandidateIdAndJobPostingId(candidateId, jobId);
    }

    public List<JobPostingResponse> getMyFavorites(Long candidateId) {
        List<SavedJob> saved = savedJobRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId);

        Map<Long, JobPosting> jobsById = jobPostingRepository
                .findAllById(saved.stream().map(SavedJob::getJobPostingId).toList())
                .stream()
                .collect(Collectors.toMap(JobPosting::getId, Function.identity()));

        return saved.stream()
                .map(s -> jobsById.get(s.getJobPostingId()))
                .filter(Objects::nonNull)
                .map(jobPostingService::toResponse)
                .toList();
    }
}
