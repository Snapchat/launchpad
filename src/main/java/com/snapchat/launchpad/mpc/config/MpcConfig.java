package com.snapchat.launchpad.mpc.config;


import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MpcConfig {

    @JsonProperty("advertiser-url")
    private String advertiserUrl;

    @JsonProperty("image-name")
    private String imageName;

    public String getAdvertiserUrl() {
        return advertiserUrl;
    }

    public void setAdvertiserUrl(String advertiserUrl) {
        this.advertiserUrl = advertiserUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
