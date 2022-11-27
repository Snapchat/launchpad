package com.snapchat.launchpad.jsasset.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("conversion-relay")
@Configuration
@ConfigurationProperties("relay-asset-config")
@EnableConfigurationProperties
public class RelayAssetsConfig {
    @JsonProperty("js")
    private String js;

    @JsonProperty("js-refresh-hours")
    private int jsRefreshHours;

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
}
