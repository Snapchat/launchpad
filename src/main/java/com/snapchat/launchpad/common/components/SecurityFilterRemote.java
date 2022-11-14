package com.snapchat.launchpad.common.components;


import com.snapchat.launchpad.common.configs.AuthConfig;
import com.snapchat.launchpad.common.schemas.SnapAuthenticationToken;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("prod")
@Component("launchpadSecurityFilter")
public class SecurityFilterRemote extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(SecurityFilterRemote.class);

    private final AuthConfig authConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public SecurityFilterRemote(AuthConfig authConfig, RestTemplate restTemplate) {
        this.authConfig = authConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String bearerToken = getAuthBearerToken(request);
            String orgIdFromToken = getOrganizationId(bearerToken);
            if (Objects.equals(authConfig.getOrganizationId(), orgIdFromToken)) {
                SnapAuthenticationToken snapAuthenticationToken =
                        new SnapAuthenticationToken(orgIdFromToken, bearerToken);
                SecurityContextHolder.getContext().setAuthentication(snapAuthenticationToken);
            }
        } catch (Exception ex) {
            logger.debug("Auth failure!");
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private String getAuthBearerToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private String getOrganizationId(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        RequestEntity<Void> requestEntity =
                RequestEntity.method(HttpMethod.GET, authConfig.getIdentityProviderUrl())
                        .headers(headers)
                        .build();
        return restTemplate.exchange(requestEntity, String.class).getBody();
    }
}
