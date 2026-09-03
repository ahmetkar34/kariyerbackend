package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRole(Role role);

    @Query("""
            SELECT u FROM User u
            WHERE :keyword IS NULL OR :keyword = ''
                OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);
}
