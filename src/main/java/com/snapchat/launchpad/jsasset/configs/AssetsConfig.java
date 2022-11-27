package com.snapchat.launchpad.jsasset.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("asset-config")
@EnableConfigurationProperties
public class AssetsConfig {
    @JsonProperty("js")
    private String js;

    @JsonProperty("js-refresh-hours")
    private int jsRefreshHours;

    @JsonProperty("root-doc")
    private String rootDoc;

    public String getJs() {
        return js;
    }

    public void setJs(String js) {
        this.js = js;
    }

    public long getJsRefreshHours() {
        return jsRefreshHours;
    }

    public void setJsRefreshHours(int jsRefreshHours) {
        this.jsRefreshHours = jsRefreshHours;
    }

    public String getRootDoc() {
        return rootDoc;
    }

    public void setRootDoc(String rootDoc) {
        this.rootDoc = rootDoc;
    }
}
