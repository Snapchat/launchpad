package com.snapchat.launchpad.common.configs;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("relay-config")
public class RelayConfig {
    @JsonProperty("pixel-path")
    private String pixelPath;

    @JsonProperty("pixel-server-host")
    private String pixelServerHost;

    @JsonProperty("pixel-server-test-host")
    private String pixelServerTestHost;

    @JsonProperty("v2-conversion-path")
    private String v2conversionPath;

    public String getPixelPath() {
        return pixelPath;
    }

    public void setPixelPath(String pixelPath) {
        this.pixelPath = pixelPath;
    }

    public String getPixelServerHost() {
        return pixelServerHost;
    }

    public void setPixelServerHost(String pixelServerHost) {
        this.pixelServerHost = pixelServerHost;
    }

    public String getPixelServerTestHost() {
        return pixelServerTestHost;
    }

    public void setPixelServerTestHost(String pixelServerTestHost) {
        this.pixelServerTestHost = pixelServerTestHost;
    }

    public String getV2conversionPath() {
        return v2conversionPath;
    }

    public void setV2conversionPath(String v2conversionPath) {
        this.v2conversionPath = v2conversionPath;
    }
}
