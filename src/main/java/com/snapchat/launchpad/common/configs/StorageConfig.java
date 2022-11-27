package com.snapchat.launchpad.common.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("conversion-log")
@Configuration
@ConfigurationProperties("storage-config")
@EnableConfigurationProperties
public class StorageConfig {
    @JsonProperty("storage-prefix")
    private String storagePrefix;

    @JsonProperty("logging-prefix")
    private String loggingPrefix;

    @JsonProperty("adhoc-prefix")
    private String adhocPrefix;

    public String getStoragePrefix() {
        return storagePrefix;
    }

    public void setStoragePrefix(String storagePrefix) {
        this.storagePrefix = storagePrefix;
    }

    public String getLoggingPrefix() {
        return loggingPrefix;
    }

    public void setLoggingPrefix(String loggingPrefix) {
        this.loggingPrefix = loggingPrefix;
    }

    public String getAdhocPrefix() {
        return adhocPrefix;
    }

    public void setAdhocPrefix(String adhocPrefix) {
        this.adhocPrefix = adhocPrefix;
    }
}
