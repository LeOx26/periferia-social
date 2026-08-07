package com.periferia.social.feed.infrastructure;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdFilterTest {

    @Test
    void reuses_the_incoming_correlation_id() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "abc-123");
        var response = new MockHttpServletResponse();

        new CorrelationIdFilter().doFilter(request, response, new MockFilterChain());

        assertEquals("abc-123", response.getHeader("X-Correlation-Id"));
    }

    @Test
    void generates_a_correlation_id_when_none_is_provided() throws Exception {
        var response = new MockHttpServletResponse();

        new CorrelationIdFilter().doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertNotNull(response.getHeader("X-Correlation-Id"));
        assertFalse(response.getHeader("X-Correlation-Id").isBlank());
    }

    /**
     * Sin esta limpieza, el hilo reutilizado del pool arrastra el id de la petición
     * anterior y los logs mienten. Es un fallo real y difícil de diagnosticar.
     */
    @Test
    void clears_the_mdc_after_the_request_to_avoid_leaking_across_threads() throws Exception {
        new CorrelationIdFilter().doFilter(new MockHttpServletRequest(),
                                           new MockHttpServletResponse(),
                                           new MockFilterChain());

        assertNull(MDC.get("correlationId"));
    }
}
