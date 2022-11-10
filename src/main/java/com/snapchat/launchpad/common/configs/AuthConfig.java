package com.snapchat.launchpad.common.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("prod")
@Configuration
@ConfigurationProperties("auth-config")
public class AuthConfig {
    @JsonProperty("organization-id")
    private String organizationId;

    @JsonProperty("identity-provider-url")
    private String identityProviderUrl;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getIdentityProviderUrl() {
        return identityProviderUrl;
    }

    public void setIdentityProviderUrl(String identityProviderUrl) {
        this.identityProviderUrl = identityProviderUrl;
    }
}
