package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public class MpcJobConfig {

    @JsonProperty("task_count")
    private int taskCount = 0;

    @JsonAnyGetter @JsonAnySetter private Map<String, Object> dynamicValues = new LinkedHashMap<>();

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public Map<String, Object> getDynamicValues() {
        return dynamicValues;
    }

    public void setDynamicValues(Map<String, Object> dynamicValues) {
        this.dynamicValues = dynamicValues;
    }
}
