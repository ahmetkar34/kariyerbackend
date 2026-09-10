package org.example.kariyerbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory fixed-window rate limiter for the auth endpoints most exposed to
 * brute-force / spam abuse (login, register, forgot-password, resend-verification).
 * Not registered as a Spring bean on purpose: it's wired directly into the security
 * filter chain in {@link org.example.kariyerbackend.config.SecurityConfig} so Spring
 * Boot's automatic Filter registration doesn't also add it to the servlet container's
 * global chain, which would double-count every request.
 * Per-instance state only - if this service is ever scaled to multiple instances,
 * this should move to a shared store (e.g. Redis) instead.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/resend-verification"
    );
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!LIMITED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + request.getRequestURI();
        if (isOverLimit(key)) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\","
                            + "\"message\":\"Çok fazla deneme yapıldı, lütfen bir süre sonra tekrar deneyin.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOverLimit(String key) {
        Window window = windows.computeIfAbsent(key, k -> new Window());
        synchronized (window) {
            long now = System.currentTimeMillis();
            if (now - window.start.get() > WINDOW_MILLIS) {
                window.start.set(now);
                window.count.set(0);
            }
            return window.count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final AtomicLong start = new AtomicLong(System.currentTimeMillis());
        private final AtomicInteger count = new AtomicInteger(0);
    }
}
