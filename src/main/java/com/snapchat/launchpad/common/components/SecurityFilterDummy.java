package com.snapchat.launchpad.common.components;


import com.snapchat.launchpad.common.schemas.SnapAuthenticationToken;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("dev")
@Component("launchpadSecurityFilter")
public class SecurityFilterDummy extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(SecurityFilterRemote.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        SnapAuthenticationToken snapAuthenticationToken = new SnapAuthenticationToken("", "");
        SecurityContextHolder.getContext().setAuthentication(snapAuthenticationToken);
        filterChain.doFilter(request, response);
    }
}
