package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.CandidateEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, Long> {

    List<CandidateEducation> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);
}
