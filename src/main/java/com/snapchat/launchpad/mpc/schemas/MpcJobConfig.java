package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MpcJobConfig {

    @JsonAnyGetter @JsonAnySetter private Map<String, Object> dynamicValues = new LinkedHashMap<>();

    public Map<String, Object> getDynamicValues() {
        return dynamicValues;
    }

    public void setDynamicValues(Map<String, Object> dynamicValues) {
        this.dynamicValues = dynamicValues;
    }
}
