package com.snapchat.launchpad.mpc.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mpc-aws")
@JsonSerialize(as = MpcBatchConfigAws.class)
@Configuration
@ConfigurationProperties("batch-config")
public class MpcBatchConfigAws extends MpcBatchConfig {

    @JsonProperty("job-queue-arn")
    private String jobQueueArn;

    @JsonProperty("job-role-arn")
    private String jobRoleArn;

    public String getJobQueueArn() {
        return jobQueueArn;
    }

    public void setJobQueueArn(String batchJobQueue) {
        this.jobQueueArn = batchJobQueue;
    }

    public String getJobRoleArn() {
        return jobRoleArn;
    }

    public void setJobRoleArn(String jobRoleArn) {
        this.jobRoleArn = jobRoleArn;
    }
}
