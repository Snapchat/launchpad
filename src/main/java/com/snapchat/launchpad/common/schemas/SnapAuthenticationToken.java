package com.snapchat.launchpad.common.schemas;


import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SnapAuthenticationToken extends AbstractAuthenticationToken {
    private final String organizationId;
    private final String bearerToken;

    public SnapAuthenticationToken(String organizationId, String bearerToken) {
        super(List.of(new SimpleGrantedAuthority("snap")));
        this.organizationId = organizationId;
        this.bearerToken = bearerToken;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return bearerToken;
    }

    @Override
    public Object getPrincipal() {
        return organizationId;
    }
}
