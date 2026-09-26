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

class ReadOnlyApiFilterTests {

    @Test
    void blocksWritesAndKeepsTheAllowHeader() throws Exception {
        OncePerRequestFilter filter = new ReadOnlyApiFilter(true, false);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/locker-stations");
        request.setServletPath("/api/locker-stations");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
        assertEquals("GET, HEAD, OPTIONS", response.getHeader("Allow"));
        assertFalse(chainCalled.get());
    }

    @Test
    void allowsReadsWhenReadOnlyModeIsEnabled() throws Exception {
        OncePerRequestFilter filter = new ReadOnlyApiFilter(true, false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/locker-stations");
        request.setServletPath("/api/locker-stations");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertTrue(chainCalled.get());
    }

    @Test
    void allowsPublicCatalogReadsButHidesReservationReads() throws Exception {
        OncePerRequestFilter filter = new ReadOnlyApiFilter(true, true);
        MockHttpServletRequest catalogRequest = new MockHttpServletRequest(
                "GET", "/api/locker-stations/12/compartments");
        catalogRequest.setServletPath("/api/locker-stations/12/compartments");
        MockHttpServletResponse catalogResponse = new MockHttpServletResponse();
        AtomicBoolean catalogChainCalled = new AtomicBoolean();

        filter.doFilter(catalogRequest, catalogResponse,
                (servletRequest, servletResponse) -> catalogChainCalled.set(true));

        assertEquals(HttpServletResponse.SC_OK, catalogResponse.getStatus());
        assertTrue(catalogChainCalled.get());

        MockHttpServletRequest reservationRequest = new MockHttpServletRequest("GET", "/api/reservations");
        reservationRequest.setServletPath("/api/reservations");
        MockHttpServletResponse reservationResponse = new MockHttpServletResponse();
        AtomicBoolean reservationChainCalled = new AtomicBoolean();

        filter.doFilter(reservationRequest, reservationResponse,
                (servletRequest, servletResponse) -> reservationChainCalled.set(true));

        assertEquals(HttpServletResponse.SC_NOT_FOUND, reservationResponse.getStatus());
        assertFalse(reservationChainCalled.get());
    }
}
