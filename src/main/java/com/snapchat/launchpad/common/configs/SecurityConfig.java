package com.snapchat.launchpad.common.configs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OncePerRequestFilter launchpadSecurityFilter;

    CorsFilter corsFilter() {
        CorsFilter filter = new CorsFilter();
        return filter;
    }

    @Autowired
    public SecurityConfig(
            @Qualifier("launchpadSecurityFilter") OncePerRequestFilter launchpadSecurityFilter) {
        this.launchpadSecurityFilter = launchpadSecurityFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // use custom filter for cors
        http =
                http.addFilterBefore(
                        corsFilter(), SessionManagementFilter.class);

        // disable CSRF
        http = http.csrf().disable();

        // set session management to stateless
        http =
                http.sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and();

        // set unauthorized requests exception handler
        http =
                http.exceptionHandling()
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .and();

        // Add Snap jwt Auth
        http.addFilterAt(launchpadSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        // set permissions on endpoints
        http.authorizeRequests().antMatchers("/v1/mpc_jobs").authenticated();

        return http.build();
    }
}
