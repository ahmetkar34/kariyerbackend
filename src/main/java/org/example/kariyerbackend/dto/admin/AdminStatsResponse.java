package org.example.kariyerbackend.dto.admin;

public record AdminStatsResponse(
        long totalUsers,
        long totalCandidates,
        long totalEmployers,
        long totalJobs,
        long totalApplications
) {
}
