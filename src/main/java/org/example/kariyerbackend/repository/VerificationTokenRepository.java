package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.TokenPurpose;
import org.example.kariyerbackend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByUserIdAndPurpose(Long userId, TokenPurpose purpose);

    void deleteByUserId(Long userId);
}
