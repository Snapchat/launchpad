package com.snapchat.launchpad.common.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SnapJwtTokenFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(SnapJwtTokenFilter.class);

    private final String publicUrl;

    private final String publicKeyUrl;

    public SnapJwtTokenFilter(String publicUrl, String publicKeyUrl) {
        this.publicUrl = publicUrl;
        this.publicKeyUrl = publicKeyUrl;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getAccessToken(request);
            if (validateAccessToken(token)) {
                SnapAuthenticationToken snapAuthenticationToken =
                        new SnapAuthenticationToken(token);
                SecurityContextHolder.getContext().setAuthentication(snapAuthenticationToken);
                return;
            } else {
                response.sendError(403);
            }
        } catch (Exception e) {
            logger.info("Failed to validate snap jwt token...", e);
        }
        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header.split(" ")[1].trim();
    }

    private boolean validateAccessToken(String token) throws IOException {
        @SuppressWarnings("rawtypes")
        Jwt<Header, Claims> jwt = Jwts.parser().setSigningKey(getJwks()).parseClaimsJwt(token);
        return Objects.equals(jwt.getBody().getAudience(), publicUrl)
                && jwt.getBody().getExpiration().getTime() < new Date().getTime();
    }

    @Cacheable
    private String getJwks() throws IOException {
        return new Scanner(new URL(publicKeyUrl).openStream(), StandardCharsets.UTF_8)
                .useDelimiter("\\A")
                .next();
    }
}
