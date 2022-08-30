package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public class MpcJobDefinition {
    @JsonProperty("IMAGE_TAG")
    private String imageTag;

    @JsonProperty("COMMAND")
    private String command;

    @JsonAnyGetter @JsonAnySetter private Map<String, Object> dynamicValues = new LinkedHashMap<>();

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getDynamicValues() {
        return dynamicValues;
    }

    public void setDynamicValues(Map<String, Object> dynamicValues) {
        this.dynamicValues = dynamicValues;
    }

    @Override
    public String toString() {
        return String.format("imageTag: %s\ncommand: %s", imageTag, command);
    }
}
