package com.snapchat.launchpad.common.configs;


import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwtSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                        .authenticationEntryPoint(
                                (request, response, ex) -> {
                                    response.sendError(
                                            HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                                })
                        .and();

        // set permissions on endpoints
        http.authorizeRequests().anyRequest().authenticated();

        // custom jwt decoder
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri(jwtSetUri)));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // public endpoints
        return (web) ->
                web.ignoring()
                        .antMatchers("/v2/conversion")
                        .antMatchers("/conversion")
                        .antMatchers("/conversion/validate")
                        .antMatchers("/r")
                        .antMatchers("/gateway/p")
                        .antMatchers("/static/scevent.min.js")
                        .antMatchers("/s.js")
                        .antMatchers("/health")
                        .antMatchers("/");
    }
}
