package com.snapchat.launchpad.mpc.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mpc-aws | mpc-gcp")
@JsonSerialize(as = MpcUIConfig.class)
@Configuration
@ConfigurationProperties("ui-config")
@EnableConfigurationProperties
public class MpcUIConfig {
    @JsonProperty("root-doc")
    private String rootDoc;

    public String getRootDoc() {
        return rootDoc;
    }

    public void setRootDoc(String rootDoc) {
        this.rootDoc = rootDoc;
    }
}
