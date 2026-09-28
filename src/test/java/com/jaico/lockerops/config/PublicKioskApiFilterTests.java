package com.jaico.lockerops.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

class PublicKioskApiFilterTests {

    @Test
    void allowsCustomerFacingKioskOperations() throws Exception {
        OncePerRequestFilter filter = new PublicKioskApiFilter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/payments/simulate");
        request.setServletPath("/api/payments/simulate");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertTrue(chainCalled.get());
    }

    @Test
    void hidesAdministrativeAndCollectionEndpoints() throws Exception {
        OncePerRequestFilter filter = new PublicKioskApiFilter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/locker-stations");
        request.setServletPath("/api/locker-stations");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertFalse(chainCalled.get());
    }

    @Test
    void letsLocalDevelopmentUseTheFullApi() throws Exception {
        OncePerRequestFilter filter = new PublicKioskApiFilter(false);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/locker-stations");
        request.setServletPath("/api/locker-stations");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertTrue(chainCalled.get());
    }

    @Test
    void allowsCorsPreflightRequests() throws Exception {
        OncePerRequestFilter filter = new PublicKioskApiFilter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/payments/simulate");
        request.setServletPath("/api/payments/simulate");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertTrue(chainCalled.get());
    }
}
