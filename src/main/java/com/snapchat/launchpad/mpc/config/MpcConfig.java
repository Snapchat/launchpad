package com.snapchat.launchpad.mpc.config;


import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MpcConfig {

    @JsonProperty("publisher-url")
    private String publisherUrl;

    @JsonProperty("image-name")
    private String imageName;

    public String getPublisherUrl() {
        return publisherUrl;
    }

    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
