package org.example.kariyerbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.common.PageResponse;
import org.example.kariyerbackend.dto.job.JobPostingRequest;
import org.example.kariyerbackend.dto.job.JobPostingResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.JobPostingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping
    public PageResponse<JobPostingResponse> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return jobPostingService.search(keyword, location, type, remote, page, size);
    }

    @GetMapping("/me")
    public List<JobPostingResponse> getMine(@AuthenticationPrincipal CustomUserDetails principal) {
        return jobPostingService.getByEmployer(principal.getUser().getId());
    }

    @GetMapping("/{id}")
    public JobPostingResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long viewerId = principal != null ? principal.getUser().getId() : null;
        return jobPostingService.getById(id, viewerId);
    }

    @PostMapping
    public ResponseEntity<JobPostingResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody JobPostingRequest request
    ) {
        JobPostingResponse response = jobPostingService.create(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public JobPostingResponse update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody JobPostingRequest request
    ) {
        return jobPostingService.update(id, principal.getUser().getId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        jobPostingService.delete(id, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
