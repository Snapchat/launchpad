package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonProperty;

public class MpcJob {
    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("job_status")
    private MpcJobStatus jobStatus;

    @JsonProperty("message")
    private String message;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public MpcJobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(MpcJobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
