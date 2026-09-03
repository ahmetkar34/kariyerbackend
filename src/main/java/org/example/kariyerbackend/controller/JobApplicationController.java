package org.example.kariyerbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.ApplicationStatusResponse;
import org.example.kariyerbackend.dto.job.JobApplicationResponse;
import org.example.kariyerbackend.dto.job.UpdateApplicationStatusRequest;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.JobApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @GetMapping("/me")
    public ApplicationStatusResponse getStatus(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return jobApplicationService.getStatus(jobId, principal.getUser().getId());
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> apply(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        JobApplicationResponse response = jobApplicationService.apply(jobId, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<JobApplicationResponse> getApplicants(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return jobApplicationService.getApplicants(jobId, principal.getUser().getId());
    }

    @PatchMapping("/{applicationId}/status")
    public JobApplicationResponse updateStatus(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        return jobApplicationService.updateStatus(jobId, applicationId, principal.getUser().getId(), request.status());
    }
}
