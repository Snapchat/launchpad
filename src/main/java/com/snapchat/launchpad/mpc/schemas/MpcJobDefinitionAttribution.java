package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MpcJobDefinitionAttribution {

    @JsonProperty("conversion_ids")
    private List<String> conversionIds;

    @JsonProperty("date_id")
    private String dateId;

    @JsonProperty("file_ids")
    private List<String> fileIds;

    @JsonProperty("click_days")
    private int clickDays;

    @JsonProperty("impression_days")
    private int impressionDays;

    public List<String> getConversionIds() {
        return conversionIds;
    }

    public void setConversionIds(List<String> conversionIds) {
        this.conversionIds = conversionIds;
    }

    public String getDateId() {
        return dateId;
    }

    public void setDateId(String dateId) {
        this.dateId = dateId;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public int getClickDays() {
        return clickDays;
    }

    public void setClickDays(int clickDays) {
        this.clickDays = clickDays;
    }

    public int getImpressionDays() {
        return impressionDays;
    }

    public void setImpressionDays(int impressionDays) {
        this.impressionDays = impressionDays;
    }
}
