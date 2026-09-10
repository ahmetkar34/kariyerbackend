package org.example.kariyerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.SavedJobService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class MyFavoritesController {

    private final SavedJobService savedJobService;

    @GetMapping("/me")
    public List<JobPostingResponse> getMyFavorites(@AuthenticationPrincipal CustomUserDetails principal) {
        return savedJobService.getMyFavorites(principal.getUser().getId());
    }
}
