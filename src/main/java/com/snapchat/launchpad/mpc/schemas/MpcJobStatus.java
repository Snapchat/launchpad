package com.snapchat.launchpad.mpc.schemas;

public enum MpcJobStatus {
    RUNNING("RUNNING"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED");

    private final String text;

    MpcJobStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
