package com.snapchat.launchpad.conversion.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("conversion-log")
@Configuration
@ConfigurationProperties("storage-config")
public class StorageConfig {
    @JsonProperty("storage-prefix")
    private String storagePrefix;

    public String getStoragePrefix() {
        return storagePrefix;
    }

    public void setStoragePrefix(String storagePrefix) {
        this.storagePrefix = storagePrefix;
    }
}
