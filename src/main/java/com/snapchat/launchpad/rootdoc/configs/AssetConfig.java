package com.snapchat.launchpad.rootdoc.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("asset-config")
@EnableConfigurationProperties
public class AssetConfig {
    @JsonProperty("root-doc")
    private String rootDoc;

    public String getRootDoc() {
        return rootDoc;
    }

    public void setRootDoc(String rootDoc) {
        this.rootDoc = rootDoc;
    }
}
