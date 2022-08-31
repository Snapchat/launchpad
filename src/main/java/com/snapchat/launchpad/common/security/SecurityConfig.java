package com.snapchat.launchpad.common.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${auth.public-url}")
    private String publicUrl;

    @Value("${auth.public-key-url}")
    private String publicKeyUrl;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

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
        http.addFilterBefore(
                new SnapJwtTokenFilter(publicUrl, publicKeyUrl),
                UsernamePasswordAuthenticationFilter.class);

        // set permissions on endpoints
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/v1/mpc_jobs").authenticated();

        return http.build();
    }
}
