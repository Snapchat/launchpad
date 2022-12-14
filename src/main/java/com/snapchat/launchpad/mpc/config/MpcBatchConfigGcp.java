package com.snapchat.launchpad.mpc.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mpc-gcp")
@JsonSerialize(as = MpcBatchConfigGcp.class)
@Configuration
@ConfigurationProperties("batch-config")
@EnableConfigurationProperties
public class MpcBatchConfigGcp extends MpcBatchConfig {
    @JsonProperty("instance-template")
    private String instanceTemplate;

    public String getInstanceTemplate() {
        return instanceTemplate;
    }

    public void setInstanceTemplate(String instanceTemplate) {
        this.instanceTemplate = instanceTemplate;
    }
}
