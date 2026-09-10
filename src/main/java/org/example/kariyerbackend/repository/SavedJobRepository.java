package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    boolean existsByCandidateIdAndJobPostingId(Long candidateId, Long jobPostingId);

    List<SavedJob> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    void deleteByCandidateIdAndJobPostingId(Long candidateId, Long jobPostingId);

    void deleteByCandidateId(Long candidateId);

    void deleteByJobPostingId(Long jobPostingId);
}
