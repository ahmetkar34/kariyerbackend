package org.example.kariyerbackend.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitingFilterTest {

    private final RateLimitingFilter filter = new RateLimitingFilter();

    @Test
    void allowsRequestsUnderTheLimit_thenBlocksFurtherAttemptsFromSameIp() throws Exception {
        FilterChain chain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = loginRequest("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }
        verify(chain, times(5)).doFilter(Mockito.any(), Mockito.any());

        MockHttpServletRequest sixthRequest = loginRequest("10.0.0.1");
        MockHttpServletResponse sixthResponse = new MockHttpServletResponse();
        filter.doFilter(sixthRequest, sixthResponse, chain);

        assertThat(sixthResponse.getStatus()).isEqualTo(429);
        verify(chain, times(5)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void tracksLimitsPerClientIpIndependently() throws Exception {
        FilterChain chain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(loginRequest("10.0.0.2"), new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse otherIpResponse = new MockHttpServletResponse();
        filter.doFilter(loginRequest("10.0.0.3"), otherIpResponse, chain);

        assertThat(otherIpResponse.getStatus()).isEqualTo(200);
        verify(chain, times(6)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void doesNotLimitPathsOutsideTheAuthAllowlist() throws Exception {
        FilterChain chain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/jobs");
            request.setRemoteAddr("10.0.0.4");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }
        verify(chain, times(10)).doFilter(Mockito.any(), Mockito.any());
    }

    private MockHttpServletRequest loginRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(ip);
        return request;
    }
}
