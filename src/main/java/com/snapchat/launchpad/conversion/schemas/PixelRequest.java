package com.snapchat.launchpad.conversion.schemas;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public class PixelRequest {
    @JsonProperty("cdid")
    private String clientDedupId;

    @JsonProperty("pid")
    private String pixelId;

    @JsonProperty("u_hem")
    private String hashedEmail;

    @JsonProperty("u_hpn")
    private String hashedPhone;

    @JsonProperty("c_hip")
    private String hashedIpAddress;

    @JsonProperty("ev")
    private String eventType;

    @JsonProperty("ts")
    private String timestamp;

    @JsonProperty("e_cur")
    private String currency;

    @JsonProperty("e_pr")
    private String price;

    @JsonProperty("ect")
    private String eventConversionType;

    @JsonAnyGetter @JsonAnySetter private Map<String, Object> dynamicValues = new LinkedHashMap<>();

    public String getClientDedupId() {
        return clientDedupId;
    }

    public void setClientDedupId(String clientDedupId) {
        this.clientDedupId = clientDedupId;
    }

    public String getPixelId() {
        return pixelId;
    }

    public void setPixelId(String pixelId) {
        this.pixelId = pixelId;
    }

    public String getHashedEmail() {
        return hashedEmail;
    }

    public void setHashedEmail(String hashedEmail) {
        this.hashedEmail = hashedEmail;
    }

    public String getHashedPhone() {
        return hashedPhone;
    }

    public void setHashedPhone(String hashedPhone) {
        this.hashedPhone = hashedPhone;
    }

    public String getHashedIpAddress() {
        return hashedIpAddress;
    }

    public void setHashedIpAddress(String hashedIpAddress) {
        this.hashedIpAddress = hashedIpAddress;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getEventConversionType() {
        return eventConversionType;
    }

    public void setEventConversionType(String eventConversionType) {
        this.eventConversionType = eventConversionType;
    }

    public Map<String, Object> getDynamicValues() {
        return dynamicValues;
    }

    public void setDynamicValues(Map<String, Object> dynamicValues) {
        this.dynamicValues = dynamicValues;
    }
}
