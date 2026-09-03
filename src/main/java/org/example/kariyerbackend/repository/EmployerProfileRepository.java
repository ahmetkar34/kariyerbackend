package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, Long> {
}
