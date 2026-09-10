package org.example.kariyerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.SavedStatusResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.SavedJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/{jobId}/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final SavedJobService savedJobService;

    @GetMapping
    public SavedStatusResponse getStatus(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return savedJobService.getStatus(jobId, principal.getUser().getId());
    }

    @PostMapping
    public ResponseEntity<Void> save(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        savedJobService.save(jobId, principal.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unsave(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        savedJobService.unsave(jobId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
