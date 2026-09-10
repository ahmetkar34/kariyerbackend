package org.example.kariyerbackend.repository;

import org.example.kariyerbackend.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByEmployerIdOrderByCreatedAtDesc(Long employerId);

    // Atomic UPDATE rather than load-increment-save, so concurrent views don't lose
    // increments to each other (two requests reading the same starting count).
    @Modifying
    @Query("UPDATE JobPosting j SET j.viewCount = j.viewCount + 1 WHERE j.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT j FROM JobPosting j
            LEFT JOIN j.tags t
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:location IS NULL OR :location = ''
                OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
            """)
    Page<JobPosting> search(@Param("keyword") String keyword, @Param("location") String location, Pageable pageable);
}
