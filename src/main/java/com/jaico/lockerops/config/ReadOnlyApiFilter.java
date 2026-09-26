package com.jaico.lockerops.config;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
public class ReadOnlyApiFilter extends OncePerRequestFilter {

    private static final Set<String> READ_METHODS = Set.of(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name());

    private static final List<Pattern> PUBLIC_CATALOG_PATHS = List.of(
            Pattern.compile("^/api/locker-stations$"),
            Pattern.compile("^/api/locker-stations/[0-9]+$"),
            Pattern.compile("^/api/locker-stations/[0-9]+/compartments$"),
            Pattern.compile("^/api/locker-compartments/[0-9]+$"));

    private final boolean readOnly;
    private final boolean publicCatalogOnly;

    public ReadOnlyApiFilter(
            @Value("${lockerops.api.read-only:false}") boolean readOnly,
            @Value("${lockerops.api.public-catalog-only:false}") boolean publicCatalogOnly) {
        this.readOnly = readOnly;
        this.publicCatalogOnly = publicCatalogOnly;
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
        if (readOnly && requestPath.startsWith("/api/")) {
            if (!READ_METHODS.contains(request.getMethod())) {
                response.setHeader("Allow", "GET, HEAD, OPTIONS");
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            if (publicCatalogOnly && PUBLIC_CATALOG_PATHS.stream()
                    .noneMatch(path -> path.matcher(requestPath).matches())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
