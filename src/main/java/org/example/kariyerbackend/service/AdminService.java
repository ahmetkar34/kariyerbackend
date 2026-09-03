package org.example.kariyerbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.dto.admin.AdminStatsResponse;
import org.example.kariyerbackend.dto.admin.AdminUserResponse;
import org.example.kariyerbackend.dto.common.PageResponse;
import org.example.kariyerbackend.entity.Role;
import org.example.kariyerbackend.entity.User;
import org.example.kariyerbackend.repository.JobApplicationRepository;
import org.example.kariyerbackend.repository.JobPostingRepository;
import org.example.kariyerbackend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public PageResponse<AdminUserResponse> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(userRepository.search(keyword, pageable).map(this::toResponse));
    }

    public void deleteUser(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kendi hesabınızı silemezsiniz");
        }
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
        userRepository.delete(user);
    }

    public void deleteJob(Long jobId) {
        if (!jobPostingRepository.existsById(jobId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "İlan bulunamadı");
        }
        jobPostingRepository.deleteById(jobId);
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.USER),
                userRepository.countByRole(Role.EMPLOYER),
                jobPostingRepository.count(),
                jobApplicationRepository.count()
        );
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
