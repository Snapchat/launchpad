package com.snapchat.launchpad.common.utils;


import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SnapAuthenticationToken extends AbstractAuthenticationToken {
    private final String jwtToekn;

    public SnapAuthenticationToken(String jwtToken) {
        super(List.of(new SimpleGrantedAuthority("snap")));
        this.jwtToekn = jwtToken;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwtToekn;
    }

    @Override
    public Object getPrincipal() {
        return "snap";
    }
}
