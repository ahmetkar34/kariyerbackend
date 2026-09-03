package org.example.kariyerbackend.dto.job;

import java.time.LocalDateTime;
import java.util.List;

public record JobPostingResponse(
        Long id,
        Long employerId,
        String title,
        String company,
        String location,
        String type,
        boolean remote,
        String salary,
        List<String> tags,
        String description,
        List<String> responsibilities,
        List<String> requirements,
        String aboutCompany,
        LocalDateTime createdAt
) {
}
