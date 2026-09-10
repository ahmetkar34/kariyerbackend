package org.example.kariyerbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security's cookie-based CSRF repository resolves the token lazily, and only
 * forces that resolution (writing the XSRF-TOKEN cookie) on requests it actually
 * validates - i.e. state-changing ones. A pure JSON SPA never renders a server-side
 * view that would trigger resolution on a GET, so without this filter the browser
 * would never receive the cookie until after the first write request already failed.
 * Forcing resolution on every request guarantees the cookie exists before it's needed.
 * Not a Spring bean on purpose - see {@link RateLimitingFilter} for why.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
