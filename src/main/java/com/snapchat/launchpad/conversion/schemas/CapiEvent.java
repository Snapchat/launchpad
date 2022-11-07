package com.snapchat.launchpad.conversion.schemas;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public class CapiEvent {
    @JsonProperty("client_dedup_id")
    private String clientDedupId = "";

    @JsonProperty("pixel_id")
    private String pixelId = "";

    @JsonProperty("app_id")
    private String appId = "";

    @JsonProperty("hashed_email")
    private String hashedEmail = "";

    @JsonProperty("hashed_phone")
    private String hashedPhone = "";

    @JsonProperty("event_type")
    private String eventType = "";

    @JsonProperty("timestamp")
    private String timestamp = "";

    @JsonProperty("currency")
    private String currency = "";

    @JsonProperty("price")
    private String price = "";

    @JsonProperty("event_conversion_type")
    private String eventConversionType = "";

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    public Map<String, Object> getDynamicValues() {
        return dynamicValues;
    }

    public void setDynamicValues(Map<String, Object> dynamicValues) {
        this.dynamicValues = dynamicValues;
    }

    public String getEventConversionType() {
        return eventConversionType;
    }

    public void setEventConversionType(String eventConversionType) {
        this.eventConversionType = eventConversionType;
    }
}
