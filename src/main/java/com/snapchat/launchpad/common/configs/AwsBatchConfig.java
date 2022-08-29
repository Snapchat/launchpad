package com.snapchat.launchpad.common.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("batch-aws")
@Configuration
@ConfigurationProperties("batch-config")
public class AwsBatchConfig {
    @JsonProperty("cloud-platform")
    private String cloudPlatform;

    @JsonProperty("execution-role-arn")
    private String executionRoleArn;

    @JsonProperty("job-role-arn")
    private String jobRoleArn;

    @JsonProperty("job-queue")
    private String jobQueue;

    @JsonProperty("volume")
    private String volume;

    public String getCloudPlatform() {
        return cloudPlatform;
    }

    public void setCloudPlatform(String cloudPlatform) {
        this.cloudPlatform = cloudPlatform;
    }

    public String getExecutionRoleArn() {
        return executionRoleArn;
    }

    public void setExecutionRoleArn(String batchExecutionRoleArn) {
        this.executionRoleArn = batchExecutionRoleArn;
    }

    public String getJobRoleArn() {
        return jobRoleArn;
    }

    public void setJobRoleArn(String batchJobRoleArn) {
        this.jobRoleArn = batchJobRoleArn;
    }

    public String getJobQueue() {
        return jobQueue;
    }

    public void setJobQueue(String batchJobQueue) {
        this.jobQueue = batchJobQueue;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getVolume() {
        return volume;
    }
}
