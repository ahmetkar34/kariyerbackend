package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findAllByOrderByCreatedAtDesc();

    List<JobPosting> findByEmployerIdOrderByCreatedAtDesc(Long employerId);
}
