package com.snapchat.launchpad.common.configs;


import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BatchConfig {
    @JsonProperty("cloud-platform")
    private String cloudPlatform;

    public String getCloudPlatform() {
        return cloudPlatform;
    }

    public void setCloudPlatform(String cloudPlatform) {
        this.cloudPlatform = cloudPlatform;
    }
}
