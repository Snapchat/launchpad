package com.snapchat.launchpad.common.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("batch-gcp")
@Configuration
@ConfigurationProperties("batch-config")
public class GcpBatchConfig {
    @JsonProperty("cloud-platform")
    private String cloudPlatform;

    @JsonProperty("project-id")
    private String projectId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("storage-bucket")
    private String storageBucket;

    @JsonProperty("instance-template")
    private String instanceTemplate;

    public String getCloudPlatform() {
        return cloudPlatform;
    }

    public void setCloudPlatform(String cloudPlatform) {
        this.cloudPlatform = cloudPlatform;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public String getInstanceTemplate() {
        return instanceTemplate;
    }

    public void setInstanceTemplate(String instanceTemplate) {
        this.instanceTemplate = instanceTemplate;
    }
}
