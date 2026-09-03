package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.CandidateCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateCertificateRepository extends JpaRepository<CandidateCertificate, Long> {

    List<CandidateCertificate> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);
}
