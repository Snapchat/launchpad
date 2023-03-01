package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MpcJobDefinitionLift {

    @JsonProperty("experiment_id")
    private String experimentId;

    @JsonProperty("date_id")
    private String dateId;

    @JsonProperty("file_ids")
    private List<String> fileIds;

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
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
}
