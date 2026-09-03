package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobPostingIdAndCandidateId(Long jobPostingId, Long candidateId);

    List<JobApplication> findByJobPostingIdOrderByCreatedAtDesc(Long jobPostingId);
}
