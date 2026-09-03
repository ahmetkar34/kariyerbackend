package org.example.kariyerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.admin.AdminStatsResponse;
import org.example.kariyerbackend.dto.admin.AdminUserResponse;
import org.example.kariyerbackend.dto.common.PageResponse;
import org.example.kariyerbackend.security.CustomUserDetails;
import org.example.kariyerbackend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/users")
    public PageResponse<AdminUserResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminService.getUsers(keyword, page, size);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        adminService.deleteUser(principal.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        adminService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
