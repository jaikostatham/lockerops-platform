package com.jaico.lockerops.config;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PublicKioskApiFilter extends OncePerRequestFilter {

    private static final List<PublicEndpoint> PUBLIC_KIOSK_ENDPOINTS = List.of(
            new PublicEndpoint(HttpMethod.GET, Pattern.compile("^/api/locker-stations$")),
            new PublicEndpoint(HttpMethod.GET, Pattern.compile("^/api/locker-stations/[0-9]+$")),
            new PublicEndpoint(HttpMethod.GET, Pattern.compile("^/api/locker-stations/[0-9]+/compartments$")),
            new PublicEndpoint(HttpMethod.GET, Pattern.compile("^/api/locker-compartments/[0-9]+$")),
            new PublicEndpoint(HttpMethod.POST, Pattern.compile("^/api/reservations$")),
            new PublicEndpoint(HttpMethod.POST, Pattern.compile("^/api/payments/simulate$")),
            new PublicEndpoint(HttpMethod.POST, Pattern.compile("^/api/access-codes/validate$"))
    );

    private final boolean publicKioskOnly;

    public PublicKioskApiFilter(
            @Value("${lockerops.api.public-kiosk-only:false}") boolean publicKioskOnly) {
        this.publicKioskOnly = publicKioskOnly;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String requestPath = servletPath.isEmpty()
                ? request.getRequestURI().substring(request.getContextPath().length())
                : servletPath;

        if (publicKioskOnly
                && requestPath.startsWith("/api/")
                && !isPublicKioskRequest(request.getMethod(), requestPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicKioskRequest(String method, String requestPath) {
        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        return PUBLIC_KIOSK_ENDPOINTS.stream()
                .anyMatch(endpoint -> endpoint.method().matches(method)
                        && endpoint.path().matcher(requestPath).matches());
    }

    private record PublicEndpoint(HttpMethod method, Pattern path) {
    }
}
