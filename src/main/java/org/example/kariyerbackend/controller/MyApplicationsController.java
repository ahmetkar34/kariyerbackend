package org.example.kariyerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.job.MyApplicationResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.JobApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class MyApplicationsController {

    private final JobApplicationService jobApplicationService;

    @GetMapping("/me")
    public List<MyApplicationResponse> getMyApplications(@AuthenticationPrincipal CustomUserDetails principal) {
        return jobApplicationService.getMyApplications(principal.getUser().getId());
    }
}
