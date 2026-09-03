package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobPostingIdAndCandidateId(Long jobPostingId, Long candidateId);

    Optional<JobApplication> findByJobPostingIdAndCandidateId(Long jobPostingId, Long candidateId);

    List<JobApplication> findByJobPostingIdOrderByCreatedAtDesc(Long jobPostingId);

    List<JobApplication> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}
