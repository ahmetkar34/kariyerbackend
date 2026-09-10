package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.JobAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {

    List<JobAlert> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    void deleteByCandidateId(Long candidateId);
}
