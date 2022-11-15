package com.snapchat.launchpad.common.components;


import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class CorsFilter extends GenericFilterBean {

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain)
            throws IOException, ServletException {

        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpServletRequest request = (HttpServletRequest) servletRequest;

        try {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            if ("OPTIONS".equals(request.getMethod())) {
                response.setHeader(
                        "Access-Control-Allow-Methods",
                        request.getHeader("Access-Control-Request-Method"));
                response.setHeader(
                        "Access-Control-Allow-Headers",
                        request.getHeader("Access-Control-Request-Headers"));
                response.setHeader("Access-Control-Max-Age", "180");
            }
        } catch (Exception e) {
            logger.error("Failed to attach CORS headers to response to preflight request...", e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
