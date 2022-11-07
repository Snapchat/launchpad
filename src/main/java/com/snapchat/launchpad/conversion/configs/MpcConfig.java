package com.snapchat.launchpad.conversion.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("conversion-mpc")
@Configuration
@ConfigurationProperties("log-config")
public class MpcConfig {
    @JsonProperty("storage-path")
    private String storagePath;

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
