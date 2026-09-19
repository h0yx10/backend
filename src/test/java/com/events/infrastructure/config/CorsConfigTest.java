package com.events.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(CorsConfig.class)
@TestPropertySource(properties = "app.cors-origin=http://localhost:4300, https://frontend-pi-olive-30.vercel.app")
class CorsConfigTest {

    @Autowired
    private CorsFilter filter;

    @Test
    void acceptsPreflightFromConfiguredOrigins() throws Exception {
        for (String origin : new String[]{"http://localhost:4300", "https://frontend-pi-olive-30.vercel.app"}) {
            for (String path : new String[]{"/api/events", "/api/today", "/api/capacity"}) {
                MockHttpServletResponse response = preflight(origin, path);
                assertThat(response.getStatus()).isEqualTo(200);
                assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
                assertThat(response.getHeader("Access-Control-Allow-Methods")).contains("GET", "PATCH", "PUT", "DELETE");
                assertThat(response.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
                assertThat(response.getHeader("Access-Control-Allow-Headers")).contains("content-type");
            }
        }
    }

    @Test
    void rejectsUnconfiguredOrigin() throws Exception {
        MockHttpServletResponse response = preflight("https://another-app.vercel.app", "/api/events");
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    private MockHttpServletResponse preflight(String origin, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", path);
        request.addHeader("Origin", origin);
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "content-type");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
